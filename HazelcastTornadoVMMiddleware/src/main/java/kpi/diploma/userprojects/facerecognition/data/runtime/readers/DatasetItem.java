package kpi.diploma.userprojects.facerecognition.data.runtime.readers;

import java.io.Serializable;
import java.nio.file.Paths;

public record DatasetItem(
        String filePath,
        String label,
        boolean isTrain
) implements Serializable {
    public String getRelativeSavePath(){
        String fileName = Paths.get(filePath).getFileName().toString();
        String splitFolder = isTrain ? "train" : "test";
        return  Paths.get(splitFolder, label, fileName).toString();
    }
}
