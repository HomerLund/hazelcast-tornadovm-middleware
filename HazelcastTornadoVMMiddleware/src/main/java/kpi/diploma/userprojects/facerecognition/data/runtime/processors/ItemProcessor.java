package kpi.diploma.userprojects.facerecognition.data.runtime.processors;

import java.io.Serializable;

public interface ItemProcessor<I, O> extends Serializable {
    O process(I input);
}
