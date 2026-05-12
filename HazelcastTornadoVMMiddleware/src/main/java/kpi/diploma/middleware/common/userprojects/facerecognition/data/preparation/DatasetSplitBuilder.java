package kpi.diploma.middleware.common.userprojects.facerecognition.data.preparation;

import kpi.diploma.middleware.common.userprojects.facerecognition.data.raw.*;
import kpi.diploma.middleware.common.userprojects.facerecognition.data.runtime.DatasetSplitReader;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


// Divining data into training and test parts and saves to disk
public class DatasetSplitBuilder implements Serializable {
    private final RawDataReader dataReader;
    private final double trainingRatio;
    private final String keyFaceWord;
    private final String baseTargetFolderPath;

    public DatasetSplitBuilder(RawDataReader dataReader, double trainingRatio, String keyFaceWord, String baseTargetFolderPath){
        this.dataReader = dataReader;
        this.trainingRatio = trainingRatio;
        this.keyFaceWord = keyFaceWord;
        this.baseTargetFolderPath = baseTargetFolderPath;

        try {
            Files.createDirectories(Paths.get(baseTargetFolderPath, "train", "face"));
            Files.createDirectories(Paths.get(baseTargetFolderPath, "train", "nonface"));
            Files.createDirectories(Paths.get(baseTargetFolderPath, "test", "face"));
            Files.createDirectories(Paths.get(baseTargetFolderPath, "test", "nonface"));
        }
        catch (IOException e){
            throw new RuntimeException("Error: Error creating dataset structure in folder: " + baseTargetFolderPath, e);
        }
    }

    public void buildDatasetStructure(){
        Path basePath = Paths.get(baseTargetFolderPath);

        for (RawData data : dataReader.streamRaw()){
            boolean isTrain = Math.random() < trainingRatio;
            boolean isFace = data.name().toLowerCase().contains(keyFaceWord.toLowerCase());

            Path targetFolder = basePath
                    .resolve(isTrain ? "train" : "test")
                    .resolve(isFace ? "face" : "nonface");

            try{
                Files.write(targetFolder.resolve(data.name()), data.content());
            }
            catch (IOException e){
                System.err.println("Error: Error while writing data");
            }
        }
    }
}
