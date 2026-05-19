package kpi.diploma.userprojects.facerecognition.data.preparation.share.io;

import kpi.diploma.middleware.core.data.io.RemoteTargetWriter;
import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DiskTargetWriter implements RemoteTargetWriter<DatasetItem> {
    private final String sourceSubPath;
    private final String targetBaseDirectory;


    public DiskTargetWriter(String sourceSubPath, String targetBaseDirectory) {
        this.sourceSubPath = sourceSubPath;
        this.targetBaseDirectory = targetBaseDirectory;
    }

    @Override
    public void writeToDisk(DatasetItem metadata, byte[] content, String workerNodeId) {
        try{
            String originalPath = metadata.filePath();
            String newSubPath = Paths.get(targetBaseDirectory, workerNodeId).toString();

            String modifiedPath = originalPath.replace(sourceSubPath, newSubPath);
            Path finalTargetPath = Paths.get(modifiedPath);

            Files.createDirectories(finalTargetPath.getParent());
            Files.write(finalTargetPath, content);
        } catch (IOException e) {
            throw new RuntimeException("Error: Failed to write streamed file to specific node with id: " + workerNodeId + ": ", e);
        }
    }
}
