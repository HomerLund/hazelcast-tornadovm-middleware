package kpi.diploma.userprojects.facerecognition.data.raw;

import java.io.Serializable;

public interface RawDataReader extends Serializable {
    Iterable<RawData> streamRaw();
}
