package kpi.diploma.userprojects.facerecognition.data.runtime.loaders.imageloader;

import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetItem;

import java.io.Serializable;

public record LoadedItem (
        DatasetItem metadata,
        byte[] content
) implements Serializable {}
