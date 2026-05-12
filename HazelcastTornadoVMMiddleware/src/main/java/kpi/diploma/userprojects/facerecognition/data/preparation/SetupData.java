package kpi.diploma.userprojects.facerecognition.data.preparation;

import  kpi.diploma.userprojects.facerecognition.data.raw.*;
import kpi.diploma.userprojects.facerecognition.data.raw.RawDataReader;
import kpi.diploma.userprojects.facerecognition.data.raw.SingleFolderDiskReader;

import java.nio.file.Paths;
import java.util.List;

public class SetupData {
    public static void main(String[] args){
        System.out.println("Preparing the dataset structure");

        String rawDataPath = Paths.get("userprojects", "facerecognition", "assets", "dataset", "raw", "Dataset").toString();
        List<String> extensions = List.of(".jpg", ".jpeg", ".png", ".bmp");
        RawDataReader reader = new SingleFolderDiskReader(rawDataPath, extensions);

        double trainingRatio = 0.8;
        String keyFaceWord = "human";
        String targetPath = Paths.get("userprojects", "facerecognition", "assets", "dataset").toString();

        try{
            DatasetSplitBuilder builder = new DatasetSplitBuilder(reader, trainingRatio, keyFaceWord, targetPath);

            builder.buildDatasetStructure();

            System.out.println("The dataset structure has been successfully generated");
            System.out.println("Path to the folder: " + targetPath);
        }
        catch (Exception e){
            System.err.println("An error occurred while creating the dataset structure");
            e.printStackTrace();
        }
    }
}
