package kpi.diploma.userprojects.facerecognition.model.io;

import kpi.diploma.userprojects.facerecognition.model.core.NeuralNetwork;

import java.io.*;

public class ModelSerializer {
    public static void saveModel(NeuralNetwork network, String filePath){
        try(ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(filePath))){
            stream.writeObject(network);
            System.out.println("The model has been successfully saved to a file: " + filePath);
        }
        catch (IOException e){
            System.err.println("Error: The save failed: " + e.getMessage());
        }
    }

    public static NeuralNetwork loadModel(String filePath){
        try(ObjectInputStream stream = new ObjectInputStream(new FileInputStream(filePath))){
            NeuralNetwork network = (NeuralNetwork) stream.readObject();
            System.out.println("The model has been successfully downloaded from the file: " + filePath);
            return network;
        }
        catch (IOException | ClassNotFoundException e){
            System.err.println("Error: The download failed: " + e.getMessage());
            return null;
        }
    }
}
