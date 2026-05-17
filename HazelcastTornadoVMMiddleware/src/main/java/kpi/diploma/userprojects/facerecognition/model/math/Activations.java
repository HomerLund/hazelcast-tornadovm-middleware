package kpi.diploma.userprojects.facerecognition.model.math;

public class Activations {
    public static void reluForward(float[] input, float[] output, int length){
        for (int i = 0; i < length; i++) {
            output[i] = Math.max(0.0f, input[i]);
        }
    }

    public static void reluBackward(float[] inputCache, float[] outputGradient, float[] inputGradient, int length){
        for (int i = 0; i < length; i++) {
            inputGradient[i] = inputCache[i] > 0.0f ? outputGradient[i] : 0.0f;
        }
    }

    public static void sigmoidForward(float[] input, float[] output, int length){
        for (int i = 0; i < length; i++) {
            output[i] = (float) (1.0 / (1.0 + Math.exp(-input[i])));
        }
    }

    public static void sigmoidBackward(float[] outputCache, float[] outputGradient, float[] inputGradient, int length){
        for (int i = 0; i < length; i++) {
            float sig = outputCache[i];
            inputGradient[i] = outputGradient[i] * sig * (1.0f - sig);
        }
    }
}
