package kpi.diploma.userprojects.facerecognition.data.runtime.readers;

import java.nio.file.Path;

public record DatasetItem(
        Path filePath,
        String label,
        boolean isTrain
) {}
