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

        network.addLayer(new DenseLayer(196608, 128));
        network.addLayer(new ReLULayer());
        network.addLayer(new DenseLayer(128, 1));
        network.addLayer(new SigmoidLayer());

        String datasetPath = Paths.get("userprojects", "facerecognition", "assets",
                "dataset", "data", "prepared").toString();

        SequentialTrainer trainer = new SequentialTrainer(network, datasetPath, 10);

        trainer.startTraining();
    }
}
