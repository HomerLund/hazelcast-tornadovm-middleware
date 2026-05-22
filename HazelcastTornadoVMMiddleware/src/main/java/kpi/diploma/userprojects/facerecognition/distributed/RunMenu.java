package kpi.diploma.userprojects.facerecognition.distributed;

import kpi.diploma.middleware.client.api.context.ClusterContext;
import kpi.diploma.middleware.client.hazelcast.HazelcastClusterProvider;
import kpi.diploma.middleware.client.orchestration.distribution.DistributionJob;
import kpi.diploma.middleware.view.menu.builders.ResearchConsoleBuilder;
import kpi.diploma.userprojects.facerecognition.data.preparation.share.ImageDatasetPartitioner;
import kpi.diploma.userprojects.facerecognition.data.preparation.share.io.DiskImageSourceLoader;
import kpi.diploma.userprojects.facerecognition.data.preparation.share.io.DiskTargetWriter;
import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetItem;
import kpi.diploma.userprojects.facerecognition.data.runtime.readers.DatasetSplitReader;

import java.nio.file.Paths;
import java.util.List;

public class RunMenu {
    public static void main(String[] args){
        System.setProperty("hazelcast.logging.type", "none");
        String hazelcastConfigFilePath = Paths.get("userprojects", "facerecognition", "assets", "config", "hazelcast.properties").toString();


        try (ClusterContext context = ClusterContext.create(new HazelcastClusterProvider(), hazelcastConfigFilePath)){
            String datasetPath = Paths.get("userprojects", "facerecognition", "assets",
                    "dataset", "data", "prepared").toString();
            List<String> extensions = List.of(".jpg", ".jpeg", ".png", ".bmp");
            DatasetSplitReader reader = new DatasetSplitReader(datasetPath, extensions);
            List<DatasetItem> allItems = reader.readDataset();

            String sourceSubPath = Paths.get("data", "prepared").toString();

            DistributionJob<DatasetItem> distributionJob =
                    new DistributionJob.Builder<DatasetItem>()
                            .items(allItems)
                            .partitioner(new ImageDatasetPartitioner())
                            .loader(new DiskImageSourceLoader())
                            .writer(new DiskTargetWriter(sourceSubPath, "workspace"))
                            .build();


            ResearchConsoleBuilder.create("Face Recognition Research")
                    .addStandardTask("Distribute Face Dataset to Workers", () -> {
                        context.<DatasetItem>getDataDistributor().distributeData(distributionJob);
                    })
                    .addStandardTask("Set Shut Down Signal to Cluser",  () -> {
                        context.getSystemManager().shutdownAllWorkersNode();
                    })
                    .start();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
