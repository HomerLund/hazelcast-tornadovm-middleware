package kpi.diploma.userprojects.facerecognition.data.runtime.loaders;

import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetItem;

import java.io.Serializable;

public record LoadedItem (
        DatasetItem metadata,
        byte[] content
) implements Serializable {}
