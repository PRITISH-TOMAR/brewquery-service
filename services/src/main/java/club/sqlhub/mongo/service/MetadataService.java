package club.sqlhub.mongo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import club.sqlhub.mongo.models.Metadata;
import club.sqlhub.mongo.repository.MetadataRepository;

@Service
@RequiredArgsConstructor
public class MetadataService {

    private final MetadataRepository repo;

    public Metadata getByDatasetId(String datasetId) {
        return repo.findById(datasetId).orElse(null);
    }

    public Metadata save(Metadata metadata) {
        return repo.save(metadata);
    }

    public void delete(String datasetId) {
        repo.deleteById(datasetId);
    }
}
