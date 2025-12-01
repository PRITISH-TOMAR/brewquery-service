package club.sqlhub.mongo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import club.sqlhub.mongo.models.Dataset;

@Repository
public interface DatasetRepository extends MongoRepository<Dataset, String> {
    Page<Dataset> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
