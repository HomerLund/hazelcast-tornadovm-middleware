package kpi.diploma.userprojects.facerecognition.data.runtime.readers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class DatasetSplitReader {
    private final String targetFolderPath;
    private final List<String> extensions;

    public DatasetSplitReader(String targetFolderPath, List<String> extensions) {
        this.targetFolderPath = targetFolderPath;
        this.extensions = extensions;
    }

    public List<DatasetItem> readDataset (){
        Path targetDir = Paths.get(targetFolderPath);
        List<DatasetItem> items = new ArrayList<>();

        if (!Files.exists(targetDir)){
            throw new RuntimeException("Error: Folder not found: " + targetDir);
        }

        try (Stream<Path> paths = Files.walk(targetDir)){
            paths.filter(Files::isRegularFile)
                    .filter(path -> {
                        String lowerName = path.getFileName().toString().toLowerCase();
                        return extensions.stream().anyMatch(lowerName::endsWith);
                    })
                    .forEach(filePath -> {
                        String label = filePath.getParent().getFileName().toString();
                        String splitName = filePath.getParent().getParent().getFileName().toString();
                        boolean isTrain = splitName.equalsIgnoreCase("train");

                        items.add(new DatasetItem(filePath, label, isTrain));
                    });
        }
        catch (IOException e){
            throw new RuntimeException("Error: Unable to read the dataset");
        }

        return items;
    }
}
