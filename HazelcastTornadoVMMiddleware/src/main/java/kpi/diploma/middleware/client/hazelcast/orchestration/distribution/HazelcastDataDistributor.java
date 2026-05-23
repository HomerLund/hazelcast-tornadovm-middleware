package kpi.diploma.middleware.client.hazelcast.orchestration.distribution;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import kpi.diploma.middleware.client.orchestration.distribution.ClusterDataDistributor;
import kpi.diploma.middleware.client.orchestration.distribution.DistributionJob;
import kpi.diploma.middleware.core.data.distribution.DataPartitioner;
import kpi.diploma.middleware.core.data.io.RemoteSourceLoader;
import kpi.diploma.middleware.core.data.io.RemoteTargetWriter;
import kpi.diploma.middleware.core.network.MiddlewareConstants;
import kpi.diploma.middleware.core.network.RemoteWorkspaceCleanupTask;
import kpi.diploma.middleware.core.network.RemoteWriteTask;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class HazelcastDataDistributor<T> implements ClusterDataDistributor<T> {
    private final HazelcastInstance hazelcastInstance;
    private final IExecutorService executorService;

    public HazelcastDataDistributor(HazelcastInstance hazelcastInstance){
        this.hazelcastInstance = hazelcastInstance;
        this.executorService = hazelcastInstance.getExecutorService(MiddlewareConstants.SYSTEM_POOL_NAME);
    }

    @Override
    public void distributeData(DistributionJob<T> job){
        Objects.requireNonNull(job, "Distribution job can not be null");
        Objects.requireNonNull(job.getAllItems(), "Items list can not be null");
        Objects.requireNonNull(job.getPartitioner(), "Partitioner can not be null");
        Objects.requireNonNull(job.getSourceLoader(), "Source Loader can not be null");
        Objects.requireNonNull(job.getTargetWriter(), "Target Writer can not be null");
        Objects.requireNonNull(job.getWorkspacePath(), "Workspace path can not be null");

        if (job.getAllItems().isEmpty()){
            System.out.println("[Distributor] Warning: The dataset is empty. Nothing to distribute");
            return;
        }

        List<T> allItems = job.getAllItems();
        DataPartitioner<T> partitioner = job.getPartitioner();
        RemoteSourceLoader<T> sourceLoader = job.getSourceLoader();
        RemoteTargetWriter<T> targetWriter = job.getTargetWriter();
        String workspacePath = Paths.get(job.getWorkspacePath(), MiddlewareConstants.SYSTEM_SANDBOX_FOLDER_NAME).normalize().toString();
        double[] customProportions = job.getCustomProportions();

        List<Member> members = new ArrayList<>(hazelcastInstance.getCluster().getMembers());
        int numNodes = members.size();

        if(numNodes == 0){
            throw new IllegalStateException("Error: No active Hazelcast members found in the cluster");
        }

        double[] proportions = resolveProportions(numNodes, customProportions);
        List<List<T>> distributedChunks = partitioner.partition(allItems, proportions);

        System.out.println("Phase 1: Cleanup of workspace across all cluster nodes");
        executeCleanup(members, workspacePath);

        System.out.println("Phase 2: Initializing distributed lazy streaming via Hazelcast");
        executeStreaming(numNodes, members, distributedChunks, sourceLoader, targetWriter);

        System.out.println("Data distribution across the entire cluster has been successfully completed");
    }

    private double[] resolveProportions(int numNodes, double[] customProportions){
        if (customProportions != null){
            return customProportions;
        }

        double[] proportions = new double[numNodes];
        Arrays.fill(proportions, 1.0 / numNodes);

        return proportions;
    }

    private void executeCleanup(List<Member> members, String workspacePath){
        Path basePath = Paths.get(workspacePath);
        Path fileName = basePath.getFileName();

        if (fileName == null || !fileName.toString().equals(MiddlewareConstants.SYSTEM_SANDBOX_FOLDER_NAME)){
            throw new SecurityException("Critical Security Error: Attempt to delete a non-target directory"
                    + "Expected leaf directory to be '" + MiddlewareConstants.SYSTEM_SANDBOX_FOLDER_NAME + "', but got:" + basePath);
        }

        RemoteWorkspaceCleanupTask cleanupTask = new RemoteWorkspaceCleanupTask(workspacePath);

        try{
            Map<String, List<Member>> membersByHost = members.stream()
                    .collect(Collectors.groupingBy(member -> member.getAddress().getHost()));

            List<Future<Void>> cleanupFutures = new ArrayList<>();
            for(Map.Entry<String, List<Member>> entry : membersByHost.entrySet()){
                String host = entry.getKey();

                Member leaderForHost = entry.getValue().get(0);

                System.out.println("Delegating cleanup for host " + host + " to node " + leaderForHost.getUuid());

                Future<Void> future = executorService.submitToMember(cleanupTask, leaderForHost);
                cleanupFutures.add(future);
            }

            for(Future<Void> future : cleanupFutures){
                future.get();
            }

            System.out.println("Workspace has been completely sanitised");
        }
        catch (Exception e){
            System.err.println("Critical Error during cluster cleanup: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void executeStreaming(
            int numNodes,
            List<Member> members,
            List<List<T>> distributedChunks,
            RemoteSourceLoader<T> sourceLoader,
            RemoteTargetWriter<T> targetWriter
    )
    {
        for (int i = 0; i < numNodes; i++) {
            Member targetMember = members.get(i);
            List<T> nodeChunk = distributedChunks.get(i);

            String nodeId = "node-" + (i + 1);
            String workspaceNodePath = Paths.get(MiddlewareConstants.SYSTEM_SANDBOX_FOLDER_NAME, nodeId).toString();

            System.out.println("Sending a batch of metadata ( " + nodeChunk.size() + " elements) to Node: " + targetMember.getUuid());

            for(T metadata : nodeChunk){
                byte[] fileContent = sourceLoader.loadContent(metadata);

                RemoteWriteTask<T> writeTask = new RemoteWriteTask<>(metadata, fileContent, targetWriter, workspaceNodePath);

                try{
                    Future<?> networkFuture = executorService.submitToMember(writeTask, targetMember);

                    networkFuture.get();
                }
                catch (Exception e){
                    System.err.println("Error deploying an element to a " + targetMember.getUuid() + " node: " + e.getMessage());
                }
            }
        }
    }
}
