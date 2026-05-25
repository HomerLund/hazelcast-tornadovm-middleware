package kpi.diploma.userprojects.facerecognition.distributed;

import kpi.diploma.middleware.client.api.compute.ComputeJobBuilder;
import kpi.diploma.middleware.client.api.context.ClusterContext;
import kpi.diploma.middleware.client.hazelcast.HazelcastClusterProvider;
import kpi.diploma.middleware.client.orchestration.compute.ComputeJob;
import kpi.diploma.middleware.client.orchestration.distribution.DistributionJob;
import kpi.diploma.middleware.core.logging.Logger;
import kpi.diploma.middleware.view.menu.builders.ResearchConsoleBuilder;
import kpi.diploma.userprojects.facerecognition.data.preparation.share.ImageDatasetPartitioner;
import kpi.diploma.userprojects.facerecognition.data.preparation.share.io.DiskImageSourceLoader;
import kpi.diploma.userprojects.facerecognition.data.preparation.share.io.DiskTargetWriter;
import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetItem;
import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetSplitReader;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class RunMenu {
    public static void main(String[] args){
        System.setProperty("hazelcast.logging.type", "none");
        String hazelcastConfigFilePath = Paths.get("userprojects", "facerecognition", "assets", "config", "hazelcast.properties").toString();

        try (ClusterContext context = ClusterContext.create(new HazelcastClusterProvider(), hazelcastConfigFilePath)){
            String targetDirectoryPath = Paths.get("userprojects", "facerecognition", "assets",
                    "dataset", "workspace").toString();
            List<String> extensions = List.of(".jpg", ".jpeg", ".png", ".bmp");

            ResearchConsoleBuilder.create("Face Recognition Research")
                    .addBenchmarkTask(
                            "Distribute Face Dataset to Workers",
                            () -> {
                                distributeData(context, targetDirectoryPath, extensions);
                            })
                    .addBenchmarkTask(
                            "Initialise Node Caches (Phase 1 Setup)",
                            () -> {
                                initNodeCache(context, targetDirectoryPath, extensions);
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

    public static void initNodeCache(ClusterContext context, String targetDirectoryPath, List<String> extensions){
        ComputeJob<Void> setupTrainJob = ComputeJobBuilder.<String, List<DatasetItem>>create()
                .sourceFromWorkerDisks(targetDirectoryPath)
                .routeTo("cpu-engine")
                .userMethod(finalPath -> {
                    DatasetSplitReader nodeReader = new DatasetSplitReader(finalPath, extensions);
                    return nodeReader.readDataset().stream().filter(DatasetItem::isTrain).toList();
                })
                .saveToNodeCache("trainItems")
                .buildSetupJob();

        ComputeJob<Void> setupTestJob = ComputeJobBuilder.<String, List<DatasetItem>>create()
                .sourceFromWorkerDisks(targetDirectoryPath)
                .routeTo("cpu-engine")
                .userMethod(finalPath -> {
                    DatasetSplitReader nodeReader = new DatasetSplitReader(finalPath, extensions);
                    return nodeReader.readDataset().stream().filter(item -> !item.isTrain()).toList();
                })
                .saveToNodeCache("testItems")
                .buildSetupJob();

        context.getComputeManager().executeOnAllNodes(setupTrainJob);
        context.getComputeManager().executeOnAllNodes(setupTestJob);

        Logger.success("Cache", "Train and Test items successfully cached on all nodes");

    }

    public static void testNodeRamCache(ClusterContext context){
        List<Integer> allNumbers = IntStream.rangeClosed(0, 1000).boxed().toList();

        ComputeJob<Void> setupRamJob = ComputeJobBuilder.<Void, List<Integer>>create()
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

        context.getComputeManager().executeOnAllNodes(setupRamJob);
    }

    public static void shutdownCluster(ClusterContext context){
        context.getSystemManager().shutdownAllWorkersNode();
        Logger.info("System", "Cluster is down. Terminating client application");
        System.exit(0);
    }


}
