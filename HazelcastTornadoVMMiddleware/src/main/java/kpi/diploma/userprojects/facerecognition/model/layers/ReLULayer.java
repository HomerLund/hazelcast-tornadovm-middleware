package kpi.diploma.userprojects.facerecognition.model.layers;

import kpi.diploma.userprojects.facerecognition.model.math.Activations;
import kpi.diploma.userprojects.facerecognition.model.math.LinearAlgebra;

public class ReLULayer implements Layer{
    private int currentLength = 0;
    private float[] inputCache;
    private float[] outputCache;
    private float[] inputGradient;

    private void ensureCapacity(int length){
        if (currentLength != length){
            currentLength = length;
            outputCache = new float[length];
            inputGradient = new float[length];
        }
    }

    @Override
    public float[] forward(float[] input){
        this.inputCache = input;
        ensureCapacity(input.length);

        Activations.reluForward(input, outputCache, input.length);
        return outputCache;
    }

    @Override public float[] backward(float[] outputGradient){
        Activations.reluBackward(inputCache, outputGradient, inputGradient, currentLength);
        return inputGradient;
    }

    @Override
    public void updateWeights(float learningRate){}
}
