package kpi.diploma.userprojects.facerecognition.data.runtime.loaders;

import kpi.diploma.userprojects.facerecognition.data.runtime.loaders.imageloader.ImageLoader;
import kpi.diploma.userprojects.facerecognition.data.runtime.loaders.imageloader.LoadedItem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.stream.StreamSupport;

public class PipelineTensorLoader {
    private final ImageLoader loader;
    private final String targetLabel;


    public PipelineTensorLoader(ImageLoader loader, String targetLabel) {
        this.loader = loader;
        this.targetLabel = targetLabel;
    }

    public Iterable<TensorItem> streamTensors(){
        return () -> StreamSupport.stream(loader.streamImage().spliterator(), false)
                .map(loadedItem -> {
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
        }).iterator();
    }

    private float[] convertToFloatArray(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();
        float[] pixels = new float[width * height * 3];

        int i = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                pixels[i++] = (rgb >> 16) & 0xFF;
                pixels[i++] = (rgb >> 8) & 0xFF;
                pixels[i++] = rgb & 0xFF;
            }
        }

        return pixels;
    }
}
