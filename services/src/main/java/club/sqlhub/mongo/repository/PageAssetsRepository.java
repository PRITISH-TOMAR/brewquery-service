package club.sqlhub.mongo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import club.sqlhub.mongo.models.PageAssets;

@Repository
public interface PageAssetsRepository extends MongoRepository<PageAssets, String> {
    Optional<PageAssets> findByPageKey(String pageKey);
}
