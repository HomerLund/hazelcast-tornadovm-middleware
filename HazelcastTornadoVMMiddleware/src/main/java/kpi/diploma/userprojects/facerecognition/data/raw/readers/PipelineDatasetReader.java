package kpi.diploma.userprojects.facerecognition.data.raw.readers;

import kpi.diploma.userprojects.facerecognition.data.raw.RawData;
import kpi.diploma.userprojects.facerecognition.data.raw.processors.DataProcessor;

import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

public class PipelineDatasetReader implements RawDataReader{
    private final RawDataReader reader;
    private final List<DataProcessor> processors;

    public PipelineDatasetReader(RawDataReader reader, List<DataProcessor> processors) {
        this.reader = reader;
        this.processors = processors;
    }

    @Override
    public Iterable<RawData> streamRaw(){
        return () -> StreamSupport.stream(reader.streamRaw().spliterator(), false)
                .map(data -> {
                    RawData current = data;
                    for (DataProcessor processor : processors){
                        current = processor.process(current);
                    }
                    return current;
                }).iterator();
    }
}
