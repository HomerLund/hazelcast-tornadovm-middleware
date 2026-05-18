package kpi.diploma.middleware.core.data.distribution;

import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetItem;

import java.util.List;

public interface DataPartitioner<T> {
    List<List<T>> partition(List<T> items, double[] proportions);
}
