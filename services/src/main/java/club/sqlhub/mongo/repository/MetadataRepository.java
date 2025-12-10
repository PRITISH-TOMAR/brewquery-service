package club.sqlhub.mongo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import club.sqlhub.mongo.models.Metadata;
import club.sqlhub.mongo.models.Question;

@Repository
public interface MetadataRepository extends MongoRepository<Metadata, String> {
    Optional<Metadata> findById(String datasetId);

}
