package kpi.diploma.middleware.core.network;

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
        Path workspace = Paths.get(workspacePath);

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
                        catch (Exception ignored){

                        }

                    });
        }
        catch (Exception ignored){

        }

        return null;
    }
}
