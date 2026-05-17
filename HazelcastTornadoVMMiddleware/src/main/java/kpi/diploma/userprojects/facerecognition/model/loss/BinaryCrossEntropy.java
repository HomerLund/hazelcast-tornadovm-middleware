package kpi.diploma.userprojects.facerecognition.model.loss;

import kpi.diploma.userprojects.facerecognition.model.math.LossMath;

public class BinaryCrossEntropy implements LossFunction{
    private int currentLength = 0;
    private float[] gradientBuffer;

    @Override
    public float[] calculateDerivative(float[] prediction, float[] target){
        if (currentLength != prediction.length){
            currentLength = prediction.length;
            gradientBuffer = new float[prediction.length];
        }

        LossMath.bceDerivative(prediction, target, gradientBuffer, currentLength);
        return gradientBuffer;
    }
}
