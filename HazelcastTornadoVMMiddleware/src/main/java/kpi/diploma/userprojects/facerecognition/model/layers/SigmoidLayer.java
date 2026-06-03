package kpi.diploma.userprojects.facerecognition.model.layers;

import kpi.diploma.middleware.client.api.gpu.GpuMemory;
import kpi.diploma.userprojects.facerecognition.model.math.Activations;

import java.util.Arrays;

public class SigmoidLayer implements Layer{
    private int maxCapacity = 0;
    private int currentLength = 0;

    @GpuMemory(mode = GpuMemory.TransferMode.EVERY_EXECUTION)
    private float[] outputCache;

    @GpuMemory(mode = GpuMemory.TransferMode.ONCE)
    private float[] inputGradient;

    public SigmoidLayer(int maxCapacity){
        this.maxCapacity = maxCapacity;
        this.outputCache = new float[maxCapacity];
        this.inputGradient = new float[maxCapacity];
    }

    private void ensureCapacity(int length){
        this.currentLength = length;
        if (length > maxCapacity){
            throw new IllegalArgumentException("Batch size exceeded max capacity");
        }
    }

    @Override
    public float[] forward(float[] input){
        ensureCapacity(input.length);
        Activations.sigmoidForward(input, outputCache, currentLength);
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
