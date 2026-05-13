package kpi.diploma.userprojects.facerecognition.data.raw.readers;

import kpi.diploma.userprojects.facerecognition.data.raw.RawData;

import java.io.Serializable;

public interface RawDataReader extends Serializable {
    Iterable<RawData> streamRaw();
}
