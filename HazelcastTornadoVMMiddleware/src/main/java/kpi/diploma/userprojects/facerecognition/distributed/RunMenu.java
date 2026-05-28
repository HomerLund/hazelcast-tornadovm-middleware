package kpi.diploma.userprojects.facerecognition.distributed;

import kpi.diploma.middleware.client.api.pipeline.cashe.CacheJobBuilder;
import kpi.diploma.middleware.client.api.context.ClusterContext;
import kpi.diploma.middleware.client.api.pipeline.compute.DataflowJobBuilder;
import kpi.diploma.middleware.client.hazelcast.HazelcastClusterProvider;
import kpi.diploma.middleware.client.orchestration.pipeline.jobs.CacheJob;
import kpi.diploma.middleware.client.orchestration.pipeline.jobs.ComputeJob;
import kpi.diploma.middleware.client.orchestration.distribution.DistributionJob;
import kpi.diploma.middleware.core.context.NodeLocalWorkspace;
import kpi.diploma.middleware.core.function.PipelineSink;
import kpi.diploma.middleware.core.logging.Logger;
import kpi.diploma.middleware.view.menu.builders.ResearchConsoleBuilder;
import kpi.diploma.userprojects.facerecognition.data.preparation.share.ImageDatasetPartitioner;
import kpi.diploma.userprojects.facerecognition.data.preparation.share.io.DiskImageSourceLoader;
import kpi.diploma.userprojects.facerecognition.data.preparation.share.io.DiskTargetWriter;
import kpi.diploma.userprojects.facerecognition.data.runtime.loaders.imageloader.LoadedItem;
import kpi.diploma.userprojects.facerecognition.data.runtime.processors.TensorItem;
import kpi.diploma.userprojects.facerecognition.data.runtime.processors.ToTensorProcessor;
import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetItem;
import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetSplitReader;
import kpi.diploma.userprojects.facerecognition.local.EpochMetrics;
import kpi.diploma.userprojects.facerecognition.model.core.NeuralNetwork;
import kpi.diploma.userprojects.facerecognition.model.layers.DenseLayer;
import kpi.diploma.userprojects.facerecognition.model.layers.ReLULayer;
import kpi.diploma.userprojects.facerecognition.model.layers.SigmoidLayer;
import kpi.diploma.userprojects.facerecognition.model.loss.BinaryCrossEntropy;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class RunMenu {
    public static void main(String[] args){
        System.setProperty("hazelcast.logging.type", "none");
        String hazelcastConfigFilePath = Paths.get("userprojects", "facerecognition", "assets", "config", "hazelcast.properties").toString();

        try (ClusterContext context = ClusterContext.create(new HazelcastClusterProvider(), hazelcastConfigFilePath)){
            String targetDirectoryPath = Paths.get("userprojects", "facerecognition", "assets",
                    "dataset", "workspace").toString();
            List<String> extensions = List.of(".jpg", ".jpeg", ".png", ".bmp");

            String trainItemsCacheName = "trainItems", testItemsCacheName = "testItems";

            ResearchConsoleBuilder.create("Face Recognition Research")
                    .addBenchmarkTask(
                            "Distribute Face Dataset to Workers",
                            () -> {
                                distributeData(context, targetDirectoryPath, extensions);
                            })
                    .addBenchmarkTask(
                            "Initialise Node Caches (Phase 1 Setup)",
                            () -> {
                                initNodeCache(context, targetDirectoryPath, extensions, trainItemsCacheName, testItemsCacheName);
                            })
                    .addBenchmarkTask(
                            "Start training (Phase 2 Run)",
                            () -> {
                                startTraining(context, trainItemsCacheName, testItemsCacheName);
                            })
                    .addBenchmarkTask(
                            "Ram test",
                            () -> {
                                testNodeRamCache(context);
                            })
                    .addStandardTask(
                            "Set Shut Down Signal to Cluster",
                            () -> {
                                shutdownCluster(context);
                            })
                    .start();
        }
        catch (Exception e){
            Logger.error("Client", "Error: " + e.getMessage());
        }
    }

    public static void distributeData(ClusterContext context, String targetDirectoryPath, List<String> extensions){
        String datasetPath = Paths.get("userprojects", "facerecognition", "assets",
                "dataset", "data", "prepared").toString();
        DatasetSplitReader reader = new DatasetSplitReader(datasetPath, extensions);
        List<DatasetItem> allItems = reader.readDataset();

        DistributionJob<DatasetItem> distributionJob =
                new DistributionJob.Builder<DatasetItem>()
                        .items(allItems)
                        .partitioner(new ImageDatasetPartitioner())
                        .loader(new DiskImageSourceLoader())
                        .writer(new DiskTargetWriter(targetDirectoryPath))
                        .workspace(targetDirectoryPath)
                        .build();

        context.<DatasetItem>getDataDistributor().distributeData(distributionJob);
    }

    public static void initNodeCache(ClusterContext context, String targetDirectoryPath, List<String> extensions, String trainItemsCacheName, String testItemsCacheName){
        CacheJob setupTrainJob = CacheJobBuilder.<String, List<DatasetItem>>create()
                .sourceFromWorkerDisks(targetDirectoryPath)
                .routeTo("cpu-engine")
                .userMethod(finalPath -> {
                    DatasetSplitReader nodeReader = new DatasetSplitReader(finalPath, extensions);
                    return nodeReader.readDataset().stream().filter(DatasetItem::isTrain).toList();
                })
                .saveToNodeCache(trainItemsCacheName)
                .buildSetupJob();

        CacheJob setupTestJob = CacheJobBuilder.<String, List<DatasetItem>>create()
                .sourceFromWorkerDisks(targetDirectoryPath)
                .routeTo("cpu-engine")
                .userMethod(finalPath -> {
                    DatasetSplitReader nodeReader = new DatasetSplitReader(finalPath, extensions);
                    return nodeReader.readDataset().stream().filter(item -> !item.isTrain()).toList();
                })
                .saveToNodeCache(testItemsCacheName)
                .buildSetupJob();

        context.getCacheManager().setupClusterCache(setupTrainJob);
        context.getCacheManager().setupClusterCache(setupTestJob);

        Logger.success("Cache", "Train and Test items successfully cached on all nodes");

    }

    public static void testNodeRamCache(ClusterContext context){
        List<Integer> allNumbers = IntStream.rangeClosed(0, 1000).boxed().toList();

        CacheJob setupRamJob = CacheJobBuilder.<Void, List<Integer>>create()
                .sourceFromClientRam(n -> {
                    Logger.info("RAM test", "Orchestrator passed the number of nodes: " + n);

                    List<List<Integer>> partitions = new ArrayList<>();

                    int chunkSize = (int) Math.ceil((double) allNumbers.size() / n);

                    for (int i = 0; i < n; i++) {
                        int fromIndex = i * chunkSize;
                        int toIndex = Math.min(fromIndex + chunkSize, allNumbers.size());

                        if (fromIndex >= allNumbers.size()) break;

                        List<Integer> chunk = new ArrayList<>(allNumbers.subList(fromIndex, toIndex));
                        partitions.add(chunk);
                    }

                    return partitions;
                })
                .routeTo("cpu-engine")
                .saveToNodeCache("testNumbers")
                .buildSetupJob();

        context.getCacheManager().setupClusterCache(setupRamJob);
    }

    public static void startTraining(ClusterContext context, String trainItemsCacheName, String testItemsCacheName){
        Logger.info("Training", "Starting distributed CPU training pipeline...");

        NeuralNetwork network = new NeuralNetwork(new BinaryCrossEntropy());

        network.addLayer(new DenseLayer(196608, 128));
        network.addLayer(new ReLULayer());
        network.addLayer(new DenseLayer(128, 1));
        network.addLayer(new SigmoidLayer());

        int epochs = 1;
        float learningRate = 0.00001f;
        String targetLabel = "face";

        for (int epoch = 1; epoch <= epochs ; epoch++) {
            Logger.info("Training", "--- Epoch " + epoch + "/" + epochs + "---");

            final NeuralNetwork currentNetwork = network;

            String epochTrainItemsKeyName = "trainingQueue";
            List<ComputeJob<?>> setupPipeline = DataflowJobBuilder.<List<DatasetItem>>sourceFromNodeCache(trainItemsCacheName)
                    .routeTo("io-reader")
                    .generateStream(epochTrainItemsKeyName, cachedTrainItems -> {
                        List<DatasetItem> trainItems = new ArrayList<>(cachedTrainItems);
                        java.util.Collections.shuffle(trainItems);
                        return trainItems;
                    });

            context.getComputeManager().executePipeline(setupPipeline);

            List<ComputeJob<?>> trainPipeline =
                    DataflowJobBuilder.<DatasetItem>sourceFromNodeCache(epochTrainItemsKeyName)
                            .routeTo("cpu-engine")
                            .map(datasetItem -> {
                                try {
                                    Path path = Paths.get(datasetItem.filePath());
                                    byte[] bytes = Files.readAllBytes(path);

                                    LoadedItem loadedItem = new LoadedItem(datasetItem, bytes);

                                    return new ToTensorProcessor(targetLabel).process(loadedItem);
                                }
                                catch (IOException e){
                                    throw new RuntimeException("Image read failed", e);
                                }
                            })
                            .routeTo("cpu-batcher")
                            .asBatch(32)
                            .routeTo("gpu-manager")
                            .map(batch -> {
                                int currentBatchSize = batch.size();

                                int inputSize = batch.get(0).features().length;
                                int labelSize = batch.get(0).label().length;

                                float[] flattenedInputs = new float[currentBatchSize * inputSize];
                                float[] flattenedLabels = new float[currentBatchSize * labelSize];

                                for (int i = 0; i < currentBatchSize; i++) {
                                    TensorItem item = batch.get(i);
                                    System.arraycopy(item.features(), 0, flattenedInputs, i * inputSize, inputSize);
                                    System.arraycopy(item.label(), 0, flattenedLabels, i * labelSize, labelSize);
                                }

                                float[] predictions = currentNetwork.forward(flattenedInputs);

                                currentNetwork.backward(predictions, flattenedLabels);
                                currentNetwork.updateWeights(learningRate);

                                double batchTotalLoss = 0.0;
                                int correctCount = 0;

                                for (int i = 0; i < currentBatchSize; i++) {
                                    float prediction = predictions[i];
                                    float label = flattenedLabels[i];

                                    float p = Math.max(1e-7f, Math.min(1.0f - 1e-7f, prediction));
                                    batchTotalLoss += -(label * Math.log(p) + (1 - label) * Math.log(1 - p));

                                    boolean isPredicatedPositive = prediction >= 0.5f;
                                    boolean isActualPositive = label == 1.0f;

                                    if (isPredicatedPositive == isActualPositive){
                                        correctCount++;
                                    }
                                }

                                return new EpochResult(
                                        batchTotalLoss / currentBatchSize,
                                        ((double) correctCount / currentBatchSize) * 100,
                                        currentNetwork
                                );
                            })
                            .routeTo("cpu-aggregator")
                            .sink(new PipelineSink<EpochResult, EpochResult>() {
                                private double totalLoss = 0;
                                private double totalAccuracy = 0;
                                private int batchCount = 0;
                                private EpochResult lastResult;

                                @Override
                                public void process(EpochResult result){
                                    totalLoss += result.loss();
                                    totalAccuracy += result.accuracy();
                                    batchCount++;
                                    this.lastResult = result;
                                }

                                @Override
                                public EpochResult getResult(){
                                    return new EpochResult(totalLoss / batchCount, totalAccuracy / batchCount, lastResult.network());
                                }
                            });

            Map<String, EpochResult> trainResults = context.getComputeManager().executeAndGatherResults(trainPipeline);

            EpochResult nodeResult = trainResults.values().iterator().next();
            network = nodeResult.network();

            double avgLoss = trainResults.values().stream().mapToDouble(EpochResult::loss).average().orElse(0);
            double avgAccuracy = trainResults.values().stream().mapToDouble((EpochResult::accuracy)).average().orElse(0);

            Logger.success("Training", String.format("Loss: %.4f | Accuracy: %.2f%%", avgLoss, avgAccuracy));
        }

        Logger.success("Training", "Distributed Pipeline execution completed");
    }

    public static void shutdownCluster(ClusterContext context){
        context.getSystemManager().shutdownAllWorkersNode();
        Logger.info("System", "Cluster is down. Terminating client application");
        System.exit(0);
    }

    public record EpochResult (
            double loss,
            double accuracy,
            NeuralNetwork network
    ) implements Serializable {}
}
