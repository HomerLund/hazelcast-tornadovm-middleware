package kpi.diploma.userprojects.facerecognition.data.runtime.processors;

import kpi.diploma.userprojects.facerecognition.data.runtime.loaders.imageloader.LoadedItem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ToTensorProcessor implements ItemProcessor<LoadedItem, TensorItem>{
    private final String targetLabel;

    public ToTensorProcessor(String targetLabel) {
        this.targetLabel = targetLabel;
    }

    @Override
    public TensorItem process(LoadedItem loadedItem){
        try{
            ByteArrayInputStream bais = new ByteArrayInputStream(loadedItem.content());
            BufferedImage image = ImageIO.read(bais);

            float[] features = convertToFloatArray(image);

            float[] label = new float[]{
                    loadedItem.metadata().label().equalsIgnoreCase(targetLabel) ? 1.0f : 0.0f
            };

            return new TensorItem(features, label);
        }
        catch (IOException e){
            throw new RuntimeException("Error: Failed to transform photo to tensor: "
                    + loadedItem.metadata().filePath(), e);
        }
    }

    private float[] convertToFloatArray(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();

        int[] rgbArray = new int[width * height];
        image.getRGB(0, 0, width, height, rgbArray, 0, width);

        float[] pixels = new float[width * height * 3];

        int i = 0;
        for (int rgb : rgbArray) {
            pixels[i++] = ((rgb >> 16) & 0xFF) / 255.0f;
            pixels[i++] = ((rgb >> 8) & 0xFF) / 255.0f;
            pixels[i++] = (rgb & 0xFF) / 255.0f;
        }

        return pixels;
    }
}
