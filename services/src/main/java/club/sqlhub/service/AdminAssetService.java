package club.sqlhub.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import club.sqlhub.Repository.DatasetSQLRepository;
import club.sqlhub.Repository.PageAssetsSQLRepository;
import club.sqlhub.constants.MessageConstants;
import club.sqlhub.mongo.models.Dataset;
import club.sqlhub.mongo.models.PageAssets;
import club.sqlhub.utils.APiResponse.ApiResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminAssetService {

    private final DatasetSQLRepository    datasetRepository;
    private final PageAssetsSQLRepository pageAssetsRepository;
    private final RestTemplate            restTemplate;

    @Value("${supabase.storage.base-url}")
    private String supabaseStorageBaseUrl;

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    private static final String ASSETS_BUCKET = "brewquery_assets";
    private static final long   MAX_SIZE       = 5 * 1024 * 1024; // 5 MB

    // ── POST /admin/dataset/{datasetId}/cover ─────────────────────────────────

    public ResponseEntity<ApiResponse<String>> uploadDatasetCover(String datasetId, MultipartFile file) {
        return uploadAndUpdateDataset(datasetId, file, "datasets/cover/" + datasetId, "cover");
    }

    // ── POST /admin/dataset/{datasetId}/er ────────────────────────────────────

    public ResponseEntity<ApiResponse<String>> uploadDatasetEr(String datasetId, MultipartFile file) {
        return uploadAndUpdateDataset(datasetId, file, "datasets/er/" + datasetId, "er");
    }

    // ── POST /admin/page/{pageKey}/hero ───────────────────────────────────────

    public ResponseEntity<ApiResponse<String>> uploadPageHero(String pageKey, MultipartFile file) {
        try {
            if (file == null || file.isEmpty())
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.ASSET_EMPTY);
            if (file.getSize() > MAX_SIZE)
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.ASSET_TOO_LARGE);

            String publicUrl = uploadToSupabase(file, "pages/hero/" + pageKey);

            PageAssets doc = pageAssetsRepository.findByPageKey(pageKey)
                    .orElseGet(() -> { PageAssets p = new PageAssets(); p.setPageKey(pageKey); return p; });
            doc.setHeroImageUrl(publicUrl);
            doc.setUpdatedAt(LocalDateTime.now());
            pageAssetsRepository.save(doc);

            return ApiResponse.call(HttpStatus.OK, MessageConstants.ASSET_UPLOADED, publicUrl);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private ResponseEntity<ApiResponse<String>> uploadAndUpdateDataset(
            String datasetId, MultipartFile file, String objectKey, String field) {
        try {
            if (file == null || file.isEmpty())
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.ASSET_EMPTY);
            if (file.getSize() > MAX_SIZE)
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.ASSET_TOO_LARGE);

            Dataset dataset = datasetRepository.findById(datasetId);
            if (dataset == null)
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.DATASET_NOT_FOUND);

            String publicUrl = uploadToSupabase(file, objectKey);

            if ("cover".equals(field)) {
                datasetRepository.updateCoverImage(datasetId, publicUrl);
            } else {
                datasetRepository.updateErImage(datasetId, publicUrl);
            }

            return ApiResponse.call(HttpStatus.OK, MessageConstants.ASSET_UPLOADED, publicUrl);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    private String uploadToSupabase(MultipartFile file, String objectKey) throws Exception {
        String base = supabaseStorageBaseUrl.replaceAll("/+$", "");
        String storageUrl = base + "/object/" + ASSETS_BUCKET + "/" + objectKey;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseServiceRoleKey);
        headers.setContentType(MediaType.parseMediaType(
                file.getContentType() != null ? file.getContentType() : "application/octet-stream"));
        headers.set("x-upsert", "true");

        HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
        restTemplate.postForEntity(storageUrl, entity, String.class);

        return base + "/object/public/" + ASSETS_BUCKET + "/" + objectKey;
    }
}
