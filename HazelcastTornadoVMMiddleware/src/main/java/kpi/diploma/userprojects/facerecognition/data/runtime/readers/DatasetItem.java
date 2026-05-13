package kpi.diploma.userprojects.facerecognition.data.runtime.readers;

import java.io.Serializable;

public record DatasetItem(
        String filePath,
        String label,
        boolean isTrain
) implements Serializable {}
