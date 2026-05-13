package kpi.diploma.userprojects.facerecognition.data.raw.processors;

import kpi.diploma.userprojects.facerecognition.data.raw.RawData;

public interface DataProcessor{
    RawData process(RawData data);
}
