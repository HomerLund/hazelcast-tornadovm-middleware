package kpi.diploma.userprojects.facerecognition.local;

import kpi.diploma.userprojects.facerecognition.model.core.NeuralNetwork;
import kpi.diploma.userprojects.facerecognition.model.layers.DenseLayer;
import kpi.diploma.userprojects.facerecognition.model.layers.ReLULayer;
import kpi.diploma.userprojects.facerecognition.model.layers.SigmoidLayer;
import kpi.diploma.userprojects.facerecognition.model.loss.BinaryCrossEntropy;

import java.nio.file.Paths;

public class RunSequentialTrainer {
    public static void main(String[] args){
        NeuralNetwork network = new NeuralNetwork(new BinaryCrossEntropy());

        int batchSize = 1;

        network.addLayer(new DenseLayer(196608, 128, batchSize));
        network.addLayer(new ReLULayer(128 * batchSize));
        network.addLayer(new DenseLayer(128, 1, batchSize));
        network.addLayer(new SigmoidLayer(batchSize));

        String datasetPath = Paths.get("userprojects", "facerecognition", "assets",
                "dataset", "data", "prepared").toString();

        SequentialTrainer trainer = new SequentialTrainer(network, datasetPath, 1);

        trainer.startTraining();
    }
}
