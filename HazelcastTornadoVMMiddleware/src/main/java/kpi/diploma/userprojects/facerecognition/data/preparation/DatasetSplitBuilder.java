package kpi.diploma.userprojects.facerecognition.data.preparation;

import kpi.diploma.userprojects.facerecognition.data.raw.readers.RawData;
import kpi.diploma.userprojects.facerecognition.data.raw.readers.RawDataReader;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


// Divining data into training and test parts and saves to disk
public class DatasetSplitBuilder implements Serializable {
    private final RawDataReader dataReader;
    private final double trainingRatio;
    private final String keyFaceWord;
    private final String baseTargetFolderPath;
    private final String mainFolder = "prepared";

    private final List<String> subPaths = List.of(
            "train/face", "train/nonface",
            "test/face", "test/nonface"
    );

    public DatasetSplitBuilder(RawDataReader dataReader, double trainingRatio, String keyFaceWord, String baseTargetFolderPath){
        this.dataReader = dataReader;
        this.trainingRatio = trainingRatio;
        this.keyFaceWord = keyFaceWord;
        this.baseTargetFolderPath = Paths.get(baseTargetFolderPath, mainFolder).toString();
    }

    public void buildDatasetStructure(){
        Path basePath = Paths.get(baseTargetFolderPath);

        cleanOldData();
        createStructure();

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

    private void createStructure(){
        Path basePath = Paths.get(baseTargetFolderPath);

        try{
            for (String subPath : subPaths){
                Path dirPath = basePath.resolve(subPath);
                Files.createDirectories(dirPath);

                Path gitkeep = dirPath.resolve(".gitkeep");
                if (!Files.exists(gitkeep)){
                    Files.createFile(gitkeep);
                }
            }
        }
        catch(IOException e){
            throw new RuntimeException("Error: Failed to create structure");
        }
    }

    private void cleanOldData(){
        Path basePath = Paths.get(baseTargetFolderPath);

        if (!basePath.getFileName().toString().equals(mainFolder)){
            throw new SecurityException("Critical Error: Attempt to delete a non-target directory!");
        }

        if (Files.exists(basePath)){
            try(var stream = Files.walk(basePath)){
                stream.sorted(java.util.Comparator.reverseOrder())
                        .forEach(path -> {
                            try{
                                Files.delete(path);
                            }
                            catch (IOException e){
                                throw new RuntimeException("Error: Failed to delete file/folder");
                            }
                        });
            }
            catch(IOException e){
                throw new RuntimeException("Error: Failed to clean the old data");
            }
        }
    }
}
