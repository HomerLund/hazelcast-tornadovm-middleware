package kpi.diploma.userprojects.facerecognition.data.raw.readers;

import java.io.Serializable;

public interface RawDataReader extends Serializable {
    Iterable<RawData> streamRaw();
}
