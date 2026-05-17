package kpi.diploma.userprojects.facerecognition.model.math;

public class LinearAlgebra {
    public static void forwardBatch(
            float[] weights, int outputSize, int inputSize,
            float[] batchInput, int batchSize,
            float[] batchOutput
    )
    {
        for (int b = 0; b < batchSize; b++) {
            int inputOffset = b * inputSize;
            int outputOffset = b * outputSize;

            for (int out = 0; out < outputSize; out++) {
                float sum = 0;
                for (int in = 0; in < inputSize; in++) {
                    sum += weights[out * inputSize + in] * batchInput[in + inputOffset];
                }
                batchOutput[out + outputOffset] = sum;
            }
        }
    }

    public static void addBiasesBatch(
            float[] batchInput, float[] biases, float[] batchOutput,
            int batchSize, int outputSize
    )
    {
        for (int b = 0; b < batchSize; b++) {
            int offset = b * outputSize;
            for (int i = 0; i < outputSize; i++) {
                batchOutput[i + offset] = batchInput[offset + i] + biases[i];
            }
        }
    }

    public static void backwardBatch(
            float[] weights, int outputSize, int inputSize,
            float[] batchInputCache, float[] batchOutputGradient, int batchSize,
            float[] weightGradients, float[] biasGradients, float[] batchInputGradient
    )
    {
        for (int b = 0; b < batchSize; b++) {
            int inputOffset = b * inputSize;
            int outputOffset = b * outputSize;

            for (int out = 0; out < outputSize; out++) {
                float gradientOut = batchOutputGradient[out + outputOffset];
                biasGradients[out + outputOffset] = gradientOut;

                for (int in = 0; in < inputSize; in++) {
                    int weightIndex = (b * outputSize * inputSize) + (out * inputSize) + in;
                    weightGradients[weightIndex] = gradientOut * batchInputCache[in + inputOffset];
                }
            }
        }

        for (int b = 0; b < batchSize; b++) {
            int inputOffset = b * inputSize;
            int outputOffset = b * outputSize;

            for (int in = 0; in < inputSize; in++) {
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
