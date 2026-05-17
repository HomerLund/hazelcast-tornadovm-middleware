package kpi.diploma.userprojects.facerecognition.model.math;

public class LossMath {
    public static void bceDerivative(float[] prediction, float[] label, float[] gradient, int length){
        for (int i = 0; i < length; i++) {
            float p = Math.max(1e-7f, Math.min(1.0f - 1e-7f, prediction[i]));
            gradient[i] = (p - label[i]) / (p * (1.0f - p));
        }
    }
}
