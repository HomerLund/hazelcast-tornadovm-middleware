package kpi.diploma.userprojects.facerecognition.data.raw.readers;

import kpi.diploma.userprojects.facerecognition.data.raw.RawData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;


// Groups photo into two groups (faces and non-faces) and returns two lists of file paths from these groups
public class SingleFolderDiskReader implements RawDataReader{
    private final String sourcePath;
    private final List<String> extensions;

    public SingleFolderDiskReader(String sourcePath, List<String> extensions){
        this.sourcePath = sourcePath;
        this.extensions = extensions;
    }

    @Override
    public Iterable<RawData> streamRaw(){
        File folder = new File(sourcePath);

        if (!folder.exists() || !folder.isDirectory()){
            throw new IllegalArgumentException("Error: Folder not found: " + sourcePath);
        }

        File[] files = folder.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return extensions.stream().anyMatch(lowerName::endsWith);
        });

        if (files == null || files.length == 0){
            System.err.println("Warning: No files found in the folder");
            return Collections.emptyList();
        }

        return () -> new java.util.Iterator<>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext(){
                return currentIndex < files.length;
            }

            @Override
            public RawData next(){
                File file = files[currentIndex++];
                try {
                    byte[] data = Files.readAllBytes(file.toPath());
                    return new RawData(data, file.getName());
                }
                catch (IOException e){
                    throw new RuntimeException("Error: Read error");
                }
            }
        };
    }
}
