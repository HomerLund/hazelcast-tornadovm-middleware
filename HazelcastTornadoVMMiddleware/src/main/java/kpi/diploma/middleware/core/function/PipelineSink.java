package kpi.diploma.middleware.core.function;

import java.io.Serializable;

public interface PipelineSink<I, R> extends Serializable {
    void process(I item);
    R getResult();
}
