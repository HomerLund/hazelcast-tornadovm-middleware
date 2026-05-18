package kpi.diploma.userprojects.facerecognition.model.core;

import kpi.diploma.userprojects.facerecognition.model.layers.Layer;
import kpi.diploma.userprojects.facerecognition.model.loss.LossFunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NeuralNetwork implements Serializable {
    private final List<Layer> layers = new ArrayList<>();
    private final LossFunction lossFunction;

    public NeuralNetwork(LossFunction lossFunction){
        this.lossFunction = lossFunction;
    }

    public void addLayer(Layer layer){
        layers.add(layer);
    }

    public float[] forward(float[] input){
        float[] currentOutput = input;
        for (Layer layer : layers){
            currentOutput = layer.forward(currentOutput);
        }
        return currentOutput;
    }

    public void backward(float[] prediction, float[] label){
        float[] gradient = lossFunction.calculateDerivative(prediction, label);

        for (int i = layers.size() - 1; i >= 0 ; i--) {
            gradient = layers.get(i).backward(gradient);
        }
    }

    public void updateWeights(float learningRate){
        for (Layer layer : layers){
            layer.updateWeights(learningRate);
        }
    }
}
