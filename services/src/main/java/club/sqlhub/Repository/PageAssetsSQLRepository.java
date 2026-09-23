package club.sqlhub.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import club.sqlhub.mongo.models.PageAssets;
import club.sqlhub.queries.PageAssetsQueries;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PageAssetsSQLRepository {

    private final JdbcTemplate      jdbc;
    private final PageAssetsQueries queries;

    public Optional<PageAssets> findByPageKey(String pageKey) {
        try {
            PageAssets pa = jdbc.queryForObject(queries.FIND_BY_PAGE_KEY,
                    new BeanPropertyRowMapper<>(PageAssets.class), pageKey);
            return Optional.ofNullable(pa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public PageAssets save(PageAssets pa) {
        if (pa.getId() == null) pa.setId(UUID.randomUUID().toString());
        pa.setUpdatedAt(LocalDateTime.now());
        jdbc.update(queries.UPSERT, pa.getId(), pa.getPageKey(), pa.getHeroImageUrl(), pa.getUpdatedAt());
        return pa;
    }
}
