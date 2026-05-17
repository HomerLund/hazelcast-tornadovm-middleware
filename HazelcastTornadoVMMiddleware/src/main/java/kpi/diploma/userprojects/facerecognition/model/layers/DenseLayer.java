package kpi.diploma.userprojects.facerecognition.model.layers;

import kpi.diploma.userprojects.facerecognition.model.math.MatrixOperations;

import java.util.Random;

public class DenseLayer implements Layer{
    private final int inputSize;
    private final int outputSize;

    private final float[] weights;
    private final float[] biases;

    private final float[] finalWeightGradients;
    private final float[] finalBiasGradients;
    private float[] expandedWeightGradients;
    private float[] expandedBiasGradients;

    private int currentBatchSize = 0;
    private float[] inputCache;
    private float[] wXBuffer;
    private float[] outputCache;
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
        if (this.currentBatchSize != batchSize){
            this.currentBatchSize = batchSize;

            this.wXBuffer = new float[batchSize * outputSize];
            this.outputCache = new float[batchSize * outputSize];

            this.inputGradient = new float[batchSize * inputSize];

            this.expandedWeightGradients = new float[batchSize * outputSize * inputSize];
            this.expandedBiasGradients = new float[batchSize * outputSize];
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

        MatrixOperations.forwardBatch(
                weights, outputSize, inputSize,
                input, batchSize, wXBuffer
        );

        MatrixOperations.addBiasesBatch(
                wXBuffer, biases, outputCache,
                batchSize, outputSize
        );


        return outputCache;
    }

    @Override
    public float[] backward(float[] outputGradient){
        MatrixOperations.backwardBatch(
                weights, outputSize, inputSize,
                inputCache, outputGradient, currentBatchSize,
                expandedWeightGradients, expandedBiasGradients, inputGradient
        );

        MatrixOperations.averageGradients(
                expandedWeightGradients, finalWeightGradients,
                currentBatchSize, outputSize * inputSize
        );

        MatrixOperations.averageGradients(
                expandedBiasGradients, finalBiasGradients,
                currentBatchSize, outputSize
        );

        return inputGradient;
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
