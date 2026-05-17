package kpi.diploma.userprojects.facerecognition.data.runtime.pipeline;

import kpi.diploma.userprojects.facerecognition.data.runtime.loaders.imageloader.ImageLoader;
import kpi.diploma.userprojects.facerecognition.data.runtime.loaders.imageloader.LoadedItem;
import kpi.diploma.userprojects.facerecognition.data.runtime.processors.ItemProcessor;

import javax.xml.crypto.Data;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DataPipeline<T> {
    private final Stream<T> stream;

    private DataPipeline(Stream<T> stream){
        this.stream = stream;
    }

    public static DataPipeline<LoadedItem> fromLoader(ImageLoader loader){
        Stream<LoadedItem> sourceStream = StreamSupport.stream(loader.spliterator(), false);
        return new DataPipeline<>(sourceStream);
    }

    public <R> DataPipeline<R> addProcessor(ItemProcessor<T, R> processor){
        Stream<R> nextStream = this.stream.map(processor::process);
        return new DataPipeline<>(nextStream);
    }

    public Iterable<T> build(){
        return stream::iterator;
    }
}
