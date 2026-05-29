package kpi.diploma.userprojects.facerecognition.model.loss;

import kpi.diploma.userprojects.facerecognition.model.math.LossMath;

import java.util.Arrays;

public class BinaryCrossEntropy implements LossFunction{
    private int maxCapacity = 0;
    private int currentLength = 0;

    private float[] gradientBuffer;

    @Override
    public float[] calculateDerivative(float[] prediction, float[] target){
        this.currentLength = prediction.length;
        if (prediction.length > maxCapacity){
            this.maxCapacity = prediction.length;
            this.gradientBuffer = new float[maxCapacity];
        }

        LossMath.bceDerivative(prediction, target, gradientBuffer, currentLength);

        if (currentLength == maxCapacity) {
            return gradientBuffer;
        }
        else{
            return Arrays.copyOf(gradientBuffer, currentLength);
        }
    }
}
