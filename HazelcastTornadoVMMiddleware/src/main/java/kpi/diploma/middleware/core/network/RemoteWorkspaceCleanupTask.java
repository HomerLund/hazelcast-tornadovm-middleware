package kpi.diploma.middleware.core.network;

import kpi.diploma.middleware.core.logging.Logger;

import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.Callable;

public class RemoteWorkspaceCleanupTask implements Callable<Void>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String workspacePath;

    public RemoteWorkspaceCleanupTask(String workspacePath) {
        this.workspacePath = workspacePath;
    }

    @Override
    public Void call() {
        Logger.info("Cleanup", "Initializing cleanup task");

        Path workspace = Paths.get(workspacePath);
        Path fileName = workspace.getFileName();

        if (fileName == null || !fileName.toString().equals(MiddlewareConstants.SYSTEM_SANDBOX_FOLDER_NAME)){
            throw new SecurityException("Critical Security Error: Attempt to delete a non-target directory"
                    + "Expected leaf directory to be '" + MiddlewareConstants.SYSTEM_SANDBOX_FOLDER_NAME + "', but got:" + workspace);
        }

        if (!Files.exists(workspace)){
            return null;
        }

        try{
            Files.walk(workspace)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        }
                        catch (Exception e){
                            throw new RuntimeException("Failed to delete path: " + path, e);
                        }

                    });
        }
        catch (Exception e){
            throw new RuntimeException("Failed to complete workspace cleanup", e);
        }

        Logger.info("Cleanup", "Workspace has been completely sanitised");

        return null;
    }
}
