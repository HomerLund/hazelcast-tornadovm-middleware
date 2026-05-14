package kpi.diploma.userprojects.facerecognition.local;

import kpi.diploma.userprojects.facerecognition.data.runtime.loaders.PipelineTensorLoader;
import kpi.diploma.userprojects.facerecognition.data.runtime.loaders.TensorItem;
import kpi.diploma.userprojects.facerecognition.data.runtime.loaders.imageloader.ImageLoader;
import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetItem;
import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetSplitReader;
import kpi.diploma.userprojects.facerecognition.model.core.NeuralNetwork;

import java.util.List;
import java.util.stream.Collectors;

public class SequentialTrainer {
    private final NeuralNetwork network;
    private final String datasetPath;
    private final int epochs;
    private final String targetLabel = "human";

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

        int trainSize = trainItems.size();

        List<DatasetItem> testItems = allItems.stream()
                .filter(item -> !item.isTrain())
                .toList();

        PipelineTensorLoader trainLoader = new PipelineTensorLoader(new ImageLoader(trainItems), targetLabel);
        PipelineTensorLoader testLoader = new PipelineTensorLoader(new ImageLoader(testItems), targetLabel);

        for (int epoch = 1; epoch <= epochs; epoch++){
            System.out.println("Epoch " + epoch + "/" + epochs);

            double totalLoss = 0.0;
            int tensorProcessed = 0;

            for (TensorItem item : trainLoader.streamTensors()){
                tensorProcessed++;
                System.out.println("tensor " + tensorProcessed + "/" + trainSize);
            }
        }

        System.out.println("Ending sequential training");
    }
}
