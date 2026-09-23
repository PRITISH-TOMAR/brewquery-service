package club.sqlhub.mongo.service;

import org.springframework.stereotype.Service;

import club.sqlhub.Repository.MetadataSQLRepository;
import club.sqlhub.mongo.models.Metadata;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetadataService {

    private final MetadataSQLRepository repo;

    public Metadata getByDatasetId(String datasetId) {
        return repo.findById(datasetId);
    }

    public Metadata save(Metadata metadata) {
        return repo.save(metadata);
    }

    public void delete(String datasetId) {
        repo.deleteById(datasetId);
    }
}
