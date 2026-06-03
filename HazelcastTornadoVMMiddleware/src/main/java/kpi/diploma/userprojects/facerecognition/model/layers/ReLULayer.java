package kpi.diploma.userprojects.facerecognition.model.layers;

import kpi.diploma.middleware.client.api.gpu.GpuMemory;
import kpi.diploma.userprojects.facerecognition.model.math.Activations;
import kpi.diploma.userprojects.facerecognition.model.math.LinearAlgebra;

import java.util.ArrayList;
import java.util.Arrays;

public class ReLULayer implements Layer{
    private int maxCapacity = 0;
    private int currentLength = 0;

    private float[] inputCache;

    @GpuMemory(mode = GpuMemory.TransferMode.EVERY_EXECUTION)
    private final float[] outputCache;

    @GpuMemory(mode = GpuMemory.TransferMode.ONCE)
    private final float[] inputGradient;

    public ReLULayer(int maxCapacity){
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
        this.inputCache = input;
        ensureCapacity(input.length);

        Activations.reluForward(input, outputCache, currentLength);

        return outputCache;
    }

    @Override public float[] backward(float[] outputGradient){
        Activations.reluBackward(inputCache, outputGradient, inputGradient, currentLength);

        return inputGradient;
    }

    @Override
    public void updateWeights(float learningRate){}
}
