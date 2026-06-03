package kpi.diploma.userprojects.facerecognition.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RunPrediction {
    public static void main(String[] args){
        String modelDirectoryPath = Paths.get("userprojects", "facerecognition", "assets", "weights").toString();
        String imageToTest = Paths.get("userprojects", "facerecognition", "assets", "dataset", "data", "raw", "Dataset", "Human1.png").toString();

        System.out.println("File analysis: " + imageToTest);

        File folder = new File(modelDirectoryPath);
        File[] listOfFiles = folder.listFiles();
        List<String> modelPaths = new ArrayList<>();

        if (listOfFiles != null){
            for (File file : listOfFiles){
                if (file.isFile() && file.getName().startsWith("face_recognition_") && file.getName().endsWith(".model")){
                    modelPaths.add(file.getAbsolutePath());
                }
            }
        }

        float totalProbability = 0.0f;
        int activeModelsCount = 0;



        try{
            for (String modelPath : modelPaths) {
                File modelFile = new File(modelPath);
                String modelName = modelFile.getName();

                ModelPredictor predictor = new ModelPredictor(modelPath);

                float probability = predictor.predict(imageToTest);

                System.out.printf("Model [%s] analysis. Probability of a face being present: %.8f\n", modelName , probability);

                totalProbability += probability;
                activeModelsCount++;

                if (probability >= 0.5f) {
                    System.out.println("A human face was found in the photo");
                } else {
                    System.out.println("A human face was NOT found in the photo");
                }
            }
        }
        catch (FileNotFoundException e){
            System.out.println("Error: " + e.getMessage());
        }

        if (activeModelsCount > 0){
            float averageProbability = totalProbability / activeModelsCount;

            System.out.printf("Average result (%d models combined):\n", activeModelsCount);
            System.out.printf("Final probability: %.8f\n", averageProbability);

            if (averageProbability >= 0.5f) {
                System.out.println("A human face was found in the photo");
            } else {
                System.out.println("A human face was NOT found in the photo");
            }
        }
    }
}
