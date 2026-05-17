package kpi.diploma.userprojects.facerecognition.data.runtime.processors;

public record TensorItem(
        float[] features,
        float[] label
) {}
