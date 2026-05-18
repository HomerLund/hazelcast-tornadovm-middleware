package kpi.diploma.userprojects.facerecognition.data.runtime.processors;

import kpi.diploma.userprojects.facerecognition.data.runtime.loaders.imageloader.LoadedItem;
import kpi.diploma.userprojects.facerecognition.data.utils.ImageProcessingUtils;

import java.io.IOException;

public class ResizeProcessor implements ItemProcessor<LoadedItem, LoadedItem>{
    private final int targetWidth;
    private final int targetHeight;

    public ResizeProcessor(int targetWidth, int targetHeight){
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }

    @Override
    public LoadedItem process(LoadedItem input){
        try{
            byte[] resized = ImageProcessingUtils.resizeImage(input.content(), input.metadata().filePath(), targetWidth, targetHeight);
            return new LoadedItem(input.metadata(), resized);
        }
        catch(IOException e){
            throw new RuntimeException("Error: Unable to resize the loaded item");
        }
    }
}
