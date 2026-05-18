package kpi.diploma.userprojects.facerecognition.app;

import java.io.FileNotFoundException;
import java.nio.file.Paths;

public class RunPrediction {
    public static void main(String[] args){
        String modelPath = Paths.get("userprojects", "facerecognition", "assets", "weights", "face_recognition.model").toString();
        String imageToTest = Paths.get("userprojects", "facerecognition", "assets", "dataset", "data", "raw", "Dataset", "0001.png").toString();

        ModelPredictor predictor = new ModelPredictor(modelPath);

        try{
            float probability = predictor.predict(imageToTest);

            System.out.println("File analysis: " + imageToTest);
            System.out.println("Probability of a face being present: " + probability);

            if(probability >= 0.5f){
                System.out.println("A human face was found in the photo");
            }
            else{
                System.out.println("A human face was not found in the photo");
            }
        }
        catch (FileNotFoundException e){
            System.out.println("Error: " + e.getMessage());
        }
    }
}
