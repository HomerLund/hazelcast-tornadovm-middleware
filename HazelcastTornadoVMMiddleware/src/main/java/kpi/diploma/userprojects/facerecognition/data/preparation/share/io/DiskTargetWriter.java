package kpi.diploma.userprojects.facerecognition.data.preparation.share.io;

import kpi.diploma.middleware.core.data.io.RemoteTargetWriter;
import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DiskTargetWriter implements RemoteTargetWriter<DatasetItem> {
    private final String targetBaseDirectory;

    public DiskTargetWriter(String targetBaseDirectory) {
        this.targetBaseDirectory = targetBaseDirectory;
    }

    @Override
    public void writeToDisk(DatasetItem metadata, byte[] content, String workspaceNodePath) {
        try{
            String relativePath = metadata.getRelativeSavePath();
            Path finalTargetPath = Paths.get(targetBaseDirectory, workspaceNodePath, relativePath);

            Files.createDirectories(finalTargetPath.getParent());
            Files.write(finalTargetPath, content);

            System.out.println("Write file '" + finalTargetPath.getFileName() + "' to disk");
            System.out.println("File path: " + finalTargetPath);
        } catch (IOException e) {
            throw new RuntimeException("Error: Failed to write streamed file to specific node: ", e);
        }
    }
}
