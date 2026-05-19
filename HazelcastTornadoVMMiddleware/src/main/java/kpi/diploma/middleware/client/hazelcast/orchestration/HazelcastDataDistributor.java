package kpi.diploma.middleware.client.hazelcast.orchestration;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import kpi.diploma.middleware.client.orchestration.ClusterDataDistributor;
import kpi.diploma.middleware.core.data.distribution.DataPartitioner;
import kpi.diploma.middleware.core.data.io.RemoteSourceLoader;
import kpi.diploma.middleware.core.data.io.RemoteTargetWriter;
import kpi.diploma.middleware.core.network.RemoteWriteTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class HazelcastDataDistributor<T> implements ClusterDataDistributor<T> {
    private final HazelcastInstance hazelcastInstance;
    private final IExecutorService executorService;

    public HazelcastDataDistributor(HazelcastInstance hazelcastInstance){
        this.hazelcastInstance = hazelcastInstance;
        this.executorService = hazelcastInstance.getExecutorService("cluster-io-distributor");
    }

    public void distributeData(List<T> allItems,
                               DataPartitioner<T> partitioner,
                               RemoteSourceLoader<T> sourceLoader,
                               RemoteTargetWriter<T> targetWriter,
                               double[] customProportions)
    {
        List<Member> memebers = new ArrayList<>(hazelcastInstance.getCluster().getMembers());
        int numNodes = memebers.size();

        if(numNodes == 0){
            throw new IllegalStateException("Error: No active Hazelcast members found in the cluster");
        }

        double[] proportions = customProportions;
        if (proportions == null){
            proportions = new double[numNodes];
            for (int i = 0; i < numNodes; i++) {
                proportions[i] = 1.0 / numNodes;
            }
        }

        List<List<T>> distributedChunks = partitioner.partition(allItems, proportions);

        System.out.println("Initializing distributed lazy streaming via Hazelcast");

        for (int i = 0; i < numNodes; i++) {
            Member targetMember = memebers.get(i);
            List<T> nodeChunk = distributedChunks.get(i);

            System.out.println("Sending a batch of metadata ( " + nodeChunk.size() + " elements) to Node: " + targetMember.getUuid());

            for(T metadata : nodeChunk){
                byte[] fileContent = sourceLoader.loadContent(metadata);

                RemoteWriteTask<T> writeTask = new RemoteWriteTask<>(metadata, fileContent, targetWriter);

                try{
                    Future<?> networkFuture = executorService.submitToMember(writeTask, targetMember);

                    networkFuture.get();
                }
                catch (Exception e){
                    System.err.println("Error deploying an element to a " + targetMember.getUuid() + " node: " + e.getMessage());
                }
            }
        }

        System.out.println("Data distribution across the entire cluster has been successfully completed");
    }
}
