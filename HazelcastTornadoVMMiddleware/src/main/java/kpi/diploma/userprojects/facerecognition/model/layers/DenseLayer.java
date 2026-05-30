package kpi.diploma.userprojects.facerecognition.model.layers;

import kpi.diploma.middleware.client.api.gpu.GpuMemory;
import kpi.diploma.userprojects.facerecognition.model.math.LinearAlgebra;

import java.util.Arrays;
import java.util.Random;

public class DenseLayer implements Layer{
    private final int inputSize;
    private final int outputSize;

    @GpuMemory(mode = GpuMemory.TransferMode.ONCE)
    private final float[] weights;

    @GpuMemory(mode = GpuMemory.TransferMode.ONCE)
    private final float[] biases;

    @GpuMemory(mode = GpuMemory.TransferMode.EVER_EXECUTION)
    private final float[] finalWeightGradients;

    @GpuMemory(mode = GpuMemory.TransferMode.EVER_EXECUTION)
    private final float[] finalBiasGradients;

    private int maxBatchCapacity = 0;
    private int currentBatchSize = 0;

    private float[] inputCache;

    @GpuMemory(mode = GpuMemory.TransferMode.ONCE)
    private float[] wXBuffer;

    @GpuMemory(mode = GpuMemory.TransferMode.ONCE)
    private float[] outputCache;

    @GpuMemory(mode = GpuMemory.TransferMode.ONCE)
    private float[] inputGradient;

    public DenseLayer(int inputSize, int outputSize){
        this.inputSize = inputSize;
        this.outputSize = outputSize;

        this.weights = new float[outputSize * inputSize];
        this.biases = new float[outputSize];

        this.finalWeightGradients = new float[outputSize * inputSize];
        this.finalBiasGradients = new float[outputSize];

        initializeWeights();
    }

    private void ensureCapacity(int batchSize){
        this.currentBatchSize = batchSize;
        if (batchSize > maxBatchCapacity){
            this.maxBatchCapacity = batchSize;

            this.wXBuffer = new float[maxBatchCapacity * outputSize];
            this.outputCache = new float[maxBatchCapacity * outputSize];
            this.inputGradient = new float[maxBatchCapacity * inputSize];
        }
    }

    private void initializeWeights(){
        Random random = new Random();
        float limit = (float)Math.sqrt(2.0 / inputSize);
        for (int i = 0; i < weights.length; i++){
            weights[i] = (random.nextFloat() * 2 - 1) * limit;
        }
    }

    @Override
    public float[] forward(float[] input){
        this.inputCache = input;

        int batchSize = input.length / inputSize;
        ensureCapacity(batchSize);

        LinearAlgebra.forwardBatch(
                weights, outputSize, inputSize,
                input, currentBatchSize, wXBuffer
        );

        LinearAlgebra.addBiasesBatch(
                wXBuffer, biases, outputCache,
                currentBatchSize, outputSize
        );

        if (currentBatchSize == maxBatchCapacity) {
            return outputCache;
        }
        else{
            return Arrays.copyOf(outputCache, currentBatchSize * outputSize);
        }
    }

    @Override
    public float[] backward(float[] outputGradient){
        LinearAlgebra.backwardBatch(
                weights, outputSize, inputSize,
                inputCache, outputGradient, currentBatchSize,
                finalWeightGradients, finalBiasGradients, inputGradient
        );

        if (currentBatchSize == maxBatchCapacity) {
            return inputGradient;
        }
        else{
            return Arrays.copyOf(inputGradient, currentBatchSize * inputSize);
        }
    }

    @Override
    public void updateWeights(float learningRate){
        for (int i = 0; i < weights.length; i++) {
            weights[i] -= learningRate * finalWeightGradients[i];
        }

        for (int i = 0; i < biases.length; i++) {
            biases[i] -= learningRate * finalBiasGradients[i];
        }
    }
 }
