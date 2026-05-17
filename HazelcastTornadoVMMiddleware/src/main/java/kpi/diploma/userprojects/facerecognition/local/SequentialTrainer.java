package kpi.diploma.userprojects.facerecognition.local;

import kpi.diploma.userprojects.facerecognition.data.runtime.processors.TensorItem;
import kpi.diploma.userprojects.facerecognition.data.runtime.loaders.imageloader.ImageLoader;
import kpi.diploma.userprojects.facerecognition.data.runtime.pipeline.DataPipeline;
import kpi.diploma.userprojects.facerecognition.data.runtime.processors.ToTensorProcessor;
import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetItem;
import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetSplitReader;
import kpi.diploma.userprojects.facerecognition.model.core.NeuralNetwork;

import java.util.List;

public class SequentialTrainer {
    private final NeuralNetwork network;
    private final String datasetPath;
    private final int epochs;
    private final String targetLabel = "human";
    private final float learningRate = 0.01f;

    public SequentialTrainer(NeuralNetwork network, String datasetPath, int epochs){
        this.network = network;
        this.datasetPath = datasetPath;
        this.epochs = epochs;
    }

    public void startTraining() {
        System.out.println("Starting sequential training");

        List<String> extensions = List.of(".jpg", ".jpeg", ".png", ".bmp");
        DatasetSplitReader reader = new DatasetSplitReader(datasetPath, extensions);
        List<DatasetItem> allItems = reader.readDataset();

        List<DatasetItem> trainItems = allItems.stream()
                .filter(DatasetItem::isTrain)
                .toList();

        List<DatasetItem> testItems = allItems.stream()
                .filter(item -> !item.isTrain())
                .toList();

        int trainSize = trainItems.size();
        int testSize = testItems.size();

        Iterable<TensorItem> trainPipeline = DataPipeline.fromLoader(new ImageLoader(trainItems))
                .addProcessor(new ToTensorProcessor(targetLabel))
                .build();

        Iterable<TensorItem> testPipeline = DataPipeline.fromLoader(new ImageLoader(testItems))
                .addProcessor(new ToTensorProcessor(targetLabel))
                .build();

        for (int epoch = 1; epoch <= epochs; epoch++){
            System.out.println("---Epoch " + epoch + "/" + epochs + "---");

            if (trainSize > 0) {
                EpochMetrics trainMetrics = processEpoch(trainPipeline, trainSize, "Train", true);
                System.out.println("Train | Loss: " + String.format("%.4f", trainMetrics.loss())
                        + " | Accuracy: " + String.format("%.2f", trainMetrics.accuracy()) + "%");
            }

            if (testSize > 0){
                EpochMetrics testMetrics = processEpoch(testPipeline, testSize, "Test", false);
                System.out.println("Test | Loss: " + String.format("%.4f", testMetrics.loss())
                        + " | Accuracy: " + String.format("%.2f", testMetrics.accuracy()) + "%");
            }
        }

        System.out.println("--- Ending sequential training ---");
    }

    private EpochMetrics processEpoch(Iterable<TensorItem> pipeline, int datasetSize, String phaseName, boolean isTraining){
        double totalLoss = 0.0f;
        int correct = 0;
        int processed = 0;

        for (TensorItem item : pipeline){
            float[] prediction = network.forward(item.features());

            if (isTraining){
                network.backward(prediction, item.label());
                network.updateWeights(learningRate);
            }

            totalLoss += calculateLoss(prediction, item.label());
            if (isCorrect(prediction, item.label())){
                correct++;
            }

            processed++;

            System.out.println("tensor " + processed + "/" + datasetSize);
            System.out.println("\r" + phaseName + " progress: " + processed + "/" + datasetSize);
        }

        System.out.println();

        return new EpochMetrics(totalLoss / datasetSize, ((double)correct / datasetSize) * 100);
    }

    private double calculateLoss(float[] prediction, float[] label){
        float p = Math.max(1e-7f, Math.min(1.0f - 1e-7f, prediction[0]));
        return -(label[0] * Math.log(p) + (1 - label[0]) * Math.log(1 - p));
    }

    private boolean isCorrect(float[] prediction, float[] label){
        boolean isPredicatedPositive = prediction[0] >= 0.5f;
        boolean isActualPositive = label[0] == 1.0f;
        return isActualPositive == isPredicatedPositive;
    }
}
