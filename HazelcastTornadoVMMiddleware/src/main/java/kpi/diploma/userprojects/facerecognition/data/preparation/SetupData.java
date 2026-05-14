package kpi.diploma.userprojects.facerecognition.data.preparation;

import kpi.diploma.userprojects.facerecognition.data.raw.RawData;
import kpi.diploma.userprojects.facerecognition.data.raw.processors.DataProcessor;
import kpi.diploma.userprojects.facerecognition.data.raw.processors.ImageResizer;
import kpi.diploma.userprojects.facerecognition.data.raw.readers.PipelineDatasetReader;
import kpi.diploma.userprojects.facerecognition.data.raw.readers.RawDataReader;
import kpi.diploma.userprojects.facerecognition.data.raw.readers.SingleFolderDiskReader;

import java.nio.file.Paths;
import java.util.List;

public class SetupData {
    public static void main(String[] args){
        System.out.println("Preparing the dataset structure");

        String rawDataPath = Paths.get("userprojects", "facerecognition", "assets", "dataset", "data", "raw", "Dataset").toString();
        List<String> extensions = List.of(".jpg", ".jpeg", ".png", ".bmp");
        RawDataReader reader = new SingleFolderDiskReader(rawDataPath, extensions);

        List<DataProcessor> processors = List.of(new ImageResizer(256, 256));

        RawDataReader pipelineReader = new PipelineDatasetReader(reader, processors);

        double trainingRatio = 0.8;
        String keyFaceWord = "human";
        String targetPath = Paths.get("userprojects", "facerecognition", "assets", "dataset", "data").toString();

        try{
            DatasetSplitBuilder builder = new DatasetSplitBuilder(pipelineReader, trainingRatio, keyFaceWord, targetPath);

            builder.buildDatasetStructure();

            System.out.println("The dataset structure has been successfully generated");
            System.out.println("Path to the folder: " + targetPath);
        }
        catch (Exception e){
            System.err.println("An error occurred while creating the dataset structure");
            e.printStackTrace();
        }
    }
}
