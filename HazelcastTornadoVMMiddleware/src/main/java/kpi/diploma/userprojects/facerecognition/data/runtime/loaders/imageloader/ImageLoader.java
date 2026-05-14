package kpi.diploma.userprojects.facerecognition.data.runtime.loaders.imageloader;

import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetItem;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class ImageLoader implements Serializable {
    private final List<DatasetItem> itemsToLoad;


    public ImageLoader(List<DatasetItem> itemsToLoad) {
        this.itemsToLoad = itemsToLoad;
    }

    public Iterable<LoadedItem> streamImage(){
        return () -> itemsToLoad.stream().map(item -> {
            try{
                byte[] content = Files.readAllBytes(Paths.get(item.filePath()));
                return new LoadedItem(item, content);
            }
            catch(IOException e){
                throw new RuntimeException("Error: File read error: " + item.filePath(), e);
            }
        }).iterator();
    }
}
