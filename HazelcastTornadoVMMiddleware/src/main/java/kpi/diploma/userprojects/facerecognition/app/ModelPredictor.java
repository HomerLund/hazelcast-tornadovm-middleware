package kpi.diploma.userprojects.facerecognition.app;

import kpi.diploma.userprojects.facerecognition.data.runtime.loaders.imageloader.ImageLoader;
import kpi.diploma.userprojects.facerecognition.data.runtime.pipeline.DataPipeline;
import kpi.diploma.userprojects.facerecognition.data.runtime.processors.ResizeProcessor;
import kpi.diploma.userprojects.facerecognition.data.runtime.processors.TensorItem;
import kpi.diploma.userprojects.facerecognition.data.runtime.processors.ToTensorProcessor;
import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetItem;
import kpi.diploma.userprojects.facerecognition.model.core.NeuralNetwork;
import kpi.diploma.userprojects.facerecognition.model.io.ModelSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class ModelPredictor {
    private final NeuralNetwork network;
    private final String targetLabel = "face";

    public ModelPredictor(String modelPath){
        this.network = ModelSerializer.loadModel(modelPath);
        if (this.network == null){
            throw new RuntimeException("Error: Unable to load the model from the path: " + modelPath);
        }
    }

    public float predict(String imagePath) throws FileNotFoundException {
        File file = new File(imagePath);
        if (!file.exists()){
            throw new FileNotFoundException("Error: Image not found at the path: " + imagePath);
        }

        DatasetItem singleItem = new DatasetItem(imagePath, targetLabel, false);

        Iterable<TensorItem> pipeline = DataPipeline.fromLoader(new ImageLoader(List.of(singleItem)))
                .addProcessor(new ResizeProcessor(256, 256))
                .addProcessor(new ToTensorProcessor(targetLabel))
                .build();

        TensorItem tensorItem = pipeline.iterator().next();

        float[] prediction = network.forward(tensorItem.features());

        return prediction[0];
    }
}
