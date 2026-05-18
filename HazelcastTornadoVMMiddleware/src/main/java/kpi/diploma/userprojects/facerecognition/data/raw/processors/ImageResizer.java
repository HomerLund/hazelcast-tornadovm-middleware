package kpi.diploma.userprojects.facerecognition.data.raw.processors;

import kpi.diploma.userprojects.facerecognition.data.raw.RawData;
import kpi.diploma.userprojects.facerecognition.data.utils.ImageProcessingUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageResizer implements DataProcessor{
    private final int targetWidth;
    private final int targetHeight;

    public ImageResizer(int targetWidth, int targetHeight){
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }

    @Override
    public RawData process(RawData data){
        try{
            byte[] resizedBytes = ImageProcessingUtils.resizeImage(data.content(), data.name(), targetWidth, targetHeight);
            return new RawData(resizedBytes, data.name());
        }
        catch (IOException e){
            throw new RuntimeException("Error: Failed to resize image: " + data.name(), e);
        }

    }
}
