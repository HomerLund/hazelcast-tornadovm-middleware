package kpi.diploma.middleware.common.userprojects.facerecognition.data.preparation;

import  kpi.diploma.middleware.common.userprojects.facerecognition.data.raw.*;

import java.nio.file.Paths;

public class SetupData {
    public static void main(String[] args){
        System.out.println("Preparing the dataset structure");

        String rawDataPath = Paths.get("userprojects", "facerecognition", "assets", "dataset", "raw").toString();
        RawDataReader reader = new SingleFolderDiskReader(rawDataPath);

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
