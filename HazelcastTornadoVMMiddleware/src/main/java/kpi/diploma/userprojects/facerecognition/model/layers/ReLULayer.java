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

    @GpuMemory(mode = GpuMemory.TransferMode.ONCE)
    private float[] outputCache;

    @GpuMemory(mode = GpuMemory.TransferMode.ONCE)
    private float[] inputGradient;

    private void ensureCapacity(int length){
        this.currentLength = length;
        if (length > maxCapacity){
            this.maxCapacity = length;
            this.outputCache = new float[maxCapacity];
            this.inputGradient = new float[maxCapacity];
        }
    }

    @Override
    public float[] forward(float[] input){
        this.inputCache = input;
        ensureCapacity(input.length);

        Activations.reluForward(input, outputCache, currentLength);

        if (currentLength == maxCapacity) {
            return outputCache;
        }
        else{
            return Arrays.copyOf(outputCache, currentLength);
        }
    }

    @Override public float[] backward(float[] outputGradient){
        Activations.reluBackward(inputCache, outputGradient, inputGradient, currentLength);

        if (currentLength == maxCapacity) {
            return inputGradient;
        }
        else{
            return Arrays.copyOf(inputGradient, currentLength);
        }
    }

    @Override
    public void updateWeights(float learningRate){}
}
