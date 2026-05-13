package kpi.diploma.userprojects.facerecognition.data.raw.processors;

import kpi.diploma.userprojects.facerecognition.data.raw.RawData;

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
            byte[] resizedBytes = resizeImage(data.content(), data.name());
            return new RawData(resizedBytes, data.name());
        }
        catch (IOException e){
            throw new RuntimeException("Error: Failed to resize image: " + data.name(), e);
        }

    }

    private byte[] resizeImage(byte[] originalBytes, String fileName) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(originalBytes);
        BufferedImage originalImage = ImageIO.read(bais);

        if (originalImage == null){
            throw new IOException("Error: Can't read image format");
        }

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resizedImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);

        g2d.dispose();

        String format = getExtension(fileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (!ImageIO.write(resizedImage, format, baos)){
            throw new IOException("Error: Can't write image");
        }

        return baos.toByteArray();
    }

    private String getExtension(String fileName){
        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex > 0 && dotIndex < fileName.length() - 1){
            return fileName.substring(dotIndex + 1).toLowerCase();
        }

        return "jpg";
    }
}
