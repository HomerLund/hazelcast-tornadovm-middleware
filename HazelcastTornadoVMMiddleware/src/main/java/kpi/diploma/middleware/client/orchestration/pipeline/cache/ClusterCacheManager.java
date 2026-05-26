package kpi.diploma.middleware.client.orchestration.pipeline.cache;

import kpi.diploma.middleware.client.orchestration.pipeline.jobs.CacheJob;

public interface ClusterCacheManager {
    void setupClusterCache(CacheJob job);
}
