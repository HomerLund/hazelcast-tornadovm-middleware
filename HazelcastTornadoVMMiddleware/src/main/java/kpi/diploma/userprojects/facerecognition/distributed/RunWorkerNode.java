package kpi.diploma.userprojects.facerecognition.distributed;

import kpi.diploma.middleware.server.bootstrap.node.app.WorkerApplication;

import java.nio.file.Paths;

public class RunWorkerNode {
    public static void main(String[] args){
        String hazelcastConfigFilePath = Paths.get("userprojects", "facerecognition", "assets", "config", "hazelcast.properties").toString();
        WorkerApplication.run(hazelcastConfigFilePath);
    }
}
