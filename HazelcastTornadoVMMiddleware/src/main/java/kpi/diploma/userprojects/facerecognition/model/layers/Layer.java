package kpi.diploma.userprojects.facerecognition.model.layers;

import java.io.Serializable;

public interface Layer extends Serializable {
    float[] forward(float[] input);
    float[] backward(float[] outputGradient);
    void updateWeights(float learningRate);
}
