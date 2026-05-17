package kpi.diploma.userprojects.facerecognition.data.runtime.loaders;

public record TensorItem(
        float[] features,
        float[] label
) {}
