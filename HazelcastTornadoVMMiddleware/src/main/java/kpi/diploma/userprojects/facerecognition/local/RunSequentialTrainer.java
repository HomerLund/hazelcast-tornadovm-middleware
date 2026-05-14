package kpi.diploma.userprojects.facerecognition.local;

import kpi.diploma.userprojects.facerecognition.model.core.NeuralNetwork;

import java.nio.file.Paths;

public class RunSequentialTrainer {
    public static void main(String[] args){
        NeuralNetwork network = new NeuralNetwork();

        String datasetPath = Paths.get("userprojects", "facerecognition", "assets",
                "dataset", "data", "prepared").toString();

        SequentialTrainer trainer = new SequentialTrainer(network, datasetPath, 1);

        trainer.startTraining();
    }
}
