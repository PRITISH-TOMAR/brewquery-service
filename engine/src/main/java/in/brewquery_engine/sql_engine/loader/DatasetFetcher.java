package in.brewquery_engine.sql_engine.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import in.brewquery_engine.entities.CachedDatasetDTO;
import in.brewquery_engine.sql_engine.datasetcache.DatasetCacheManager;

@Component
public class DatasetFetcher {

    private static final Logger log = LoggerFactory.getLogger(DatasetFetcher.class);
    private final DatasetCacheManager cacheManager;

    public DatasetFetcher(DatasetCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public CachedDatasetDTO fetch(String datasetId) {
        try {
            return cacheManager.getDataset(datasetId);

        } catch (Exception e) {
            String msg = "Failed to fetch dataset '" + datasetId + "': " + e.getMessage();
            log.error(msg, e);
            return null;
        }
    }
}
