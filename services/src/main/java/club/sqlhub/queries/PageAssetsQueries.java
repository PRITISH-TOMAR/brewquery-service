package club.sqlhub.queries;

import org.springframework.stereotype.Component;

@Component
public class PageAssetsQueries {

    public final String FIND_BY_PAGE_KEY = """
            SELECT id::TEXT AS id, page_key AS pageKey, hero_image_url AS heroImageUrl,
                   updated_at AS updatedAt
            FROM page_assets WHERE page_key = ?
            """;

    public final String UPSERT = """
            INSERT INTO page_assets (page_key, hero_image_url, updated_at)
            VALUES (?, ?, ?)
            ON CONFLICT (page_key) DO UPDATE SET
                hero_image_url = EXCLUDED.hero_image_url,
                updated_at     = EXCLUDED.updated_at
            RETURNING id::TEXT AS id
            """;
}
