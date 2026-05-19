package kpi.diploma.userprojects.facerecognition.data.preparation.share.io;

import kpi.diploma.middleware.core.data.io.RemoteSourceLoader;
import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DiskImageSourceLoader implements RemoteSourceLoader<DatasetItem> {
    @Override
    public byte[] loadContent(DatasetItem metadata) {
        try{
            return Files.readAllBytes(Paths.get(metadata.filePath()));
        }
        catch (IOException e){
            throw new RuntimeException("Error: Failed to read file for network stream: " + metadata.filePath(), e);
        }
    }
}
