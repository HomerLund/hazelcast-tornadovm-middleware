package kpi.diploma.userprojects.facerecognition.model.loss;

import java.io.Serializable;

public interface LossFunction extends Serializable {
    float[] calculateDerivative(float[] prediction, float[] target);
}
