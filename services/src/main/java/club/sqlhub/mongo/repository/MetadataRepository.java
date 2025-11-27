package club.sqlhub.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import club.sqlhub.mongo.models.Metadata;

@Repository
public interface MetadataRepository extends MongoRepository<Metadata, String> { }
