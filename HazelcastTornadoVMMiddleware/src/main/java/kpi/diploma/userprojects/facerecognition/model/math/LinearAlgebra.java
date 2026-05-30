package kpi.diploma.userprojects.facerecognition.model.math;

import kpi.diploma.middleware.client.api.gpu.GpuKernel;
import kpi.diploma.middleware.client.api.gpu.GpuParallel;

public class LinearAlgebra {
    @GpuKernel(name = "forwardBatch")
    public static void forwardBatch(
            float[] weights, int outputSize, int inputSize,
            float[] batchInput, int batchSize,
            float[] batchOutput
    )
    {
        for (@GpuParallel int b = 0; b < batchSize; b++) {
            int inputOffset = b * inputSize;
            int outputOffset = b * outputSize;

            for (@GpuParallel int out = 0; out < outputSize; out++) {
                float sum = 0;
                for (int in = 0; in < inputSize; in++) {
                    sum += weights[out * inputSize + in] * batchInput[in + inputOffset];
                }
                batchOutput[out + outputOffset] = sum;
            }
        }
    }

    @GpuKernel(name = "addBiasesBatch")
    public static void addBiasesBatch(
            float[] batchInput, float[] biases, float[] batchOutput,
            int batchSize, int outputSize
    )
    {
        for (@GpuParallel int b = 0; b < batchSize; b++) {
            int offset = b * outputSize;
            for (@GpuParallel int i = 0; i < outputSize; i++) {
                batchOutput[i + offset] = batchInput[offset + i] + biases[i];
            }
        }
    }

    @GpuKernel(name = "backwardBatch")
    public static void backwardBatch(
            float[] weights, int outputSize, int inputSize,
            float[] batchInputCache, float[] batchOutputGradient, int batchSize,
            float[] weightGradients, float[] biasGradients, float[] batchInputGradient
    )
    {
        for (@GpuParallel int out = 0; out < outputSize; out++){
            float biasSum = 0;
            for (int b = 0; b < batchSize; b++) {
                biasSum += batchOutputGradient[b * outputSize + out];
            }
            biasGradients[out] = biasSum / batchSize;

            for (@GpuParallel int in = 0; in < inputSize; in++) {
                float weightSum = 0;
                for (int b = 0; b < batchSize; b++) {
                    weightSum += batchOutputGradient[b * outputSize + out] * batchInputCache[b * inputSize + in];
                }
                weightGradients[out * inputSize + in] = weightSum / batchSize;
            }
        }

        for (@GpuParallel int b = 0; b < batchSize; b++) {
            int inputOffset = b * inputSize;
            int outputOffset = b * outputSize;

            for (@GpuParallel int in = 0; in < inputSize; in++) {
                float sum = 0;
                for (int out = 0; out < outputSize; out++) {
                    sum += batchOutputGradient[out + outputOffset] * weights[out * inputSize + in];
                }
                batchInputGradient[in + inputOffset] = sum;
            }
        }
    }

    public static void averageGradients(
            float[] gradients, float[] outputGradients,
            int batchSize, int size
    )
    {
        for (int i = 0; i < size; i++) {
            float sum = 0;

            for (int b = 0; b < batchSize; b++) {
                sum += gradients[b * size + i];
            }

            outputGradients[i] = sum / batchSize;
        }
    }
}
