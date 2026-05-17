package kpi.diploma.userprojects.facerecognition.model.layers;

import kpi.diploma.userprojects.facerecognition.model.math.Activations;

public class SigmoidLayer implements Layer{
    private int currentLength = 0;
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
        ensureCapacity(input.length);
        Activations.sigmoidForward(input, outputCache, input.length);
        return outputCache;
    }

    @Override
    public float[] backward(float[] outputGradient){
        Activations.sigmoidBackward(outputCache, outputGradient, inputGradient, currentLength);
        return inputGradient;
    }

    @Override
    public void updateWeights(float learningRate){}
}
