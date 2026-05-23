package kpi.diploma.userprojects.facerecognition.data.preparation.share;

import kpi.diploma.middleware.core.data.distribution.DataPartitioner;
import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImageDatasetPartitioner implements DataPartitioner<DatasetItem> {
    @Override
    public List<List<DatasetItem>> partition(List<DatasetItem> items, double[] proportions){
        int numNodes = proportions.length;
        List<List<DatasetItem>> nodeChunks = new ArrayList<>();
        for (int i = 0; i < numNodes; i++) {
            nodeChunks.add(new ArrayList<>());
        }

        Map<String, List<DatasetItem>> itemsByGroup = items.stream()
                .collect(Collectors.groupingBy(item -> item.isTrain() + "_" + item.label()));

        for (Map.Entry<String, List<DatasetItem>> entry : itemsByGroup.entrySet()){
            List<DatasetItem> labelGroup = entry.getValue();

            int totalInGroup = labelGroup.size();
            int currentStartIndex = 0;

            for (int i = 0; i < numNodes; i++) {
                int chunkSize;
                if (i == numNodes - 1){
                    chunkSize = totalInGroup - currentStartIndex;
                }
                else{
                    chunkSize = (int) Math.round(totalInGroup * proportions[i]);
                }

                List<DatasetItem> slice = labelGroup.subList(currentStartIndex, currentStartIndex + chunkSize);
                nodeChunks.get(i).addAll(slice);
                currentStartIndex += chunkSize;
            }
        }
        return nodeChunks;
    }
}
