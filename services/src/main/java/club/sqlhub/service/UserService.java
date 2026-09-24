package club.sqlhub.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
import club.sqlhub.Repository.JudgeResultSQLRepository;
import club.sqlhub.Repository.QuestionSQLRepository;
import club.sqlhub.Repository.UserProfileRepository;
import club.sqlhub.constants.MessageConstants;
import club.sqlhub.entity.user.DBO.UserRawProfileDBO;
import club.sqlhub.entity.user.DTO.profile.*;
import club.sqlhub.mongo.models.Dataset;
import club.sqlhub.mongo.models.JudgeResult.JudgeResultDTO;
import club.sqlhub.mongo.models.Question;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.User.LevelUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository   profileRepo;
    private final JudgeResultSQLRepository judgeResultRepo;
    private final QuestionSQLRepository   questionRepository;
    private final DatasetSQLRepository    datasetRepository;
    private final RestTemplate            restTemplate;

    @Value("${supabase.storage.base-url}")
    private String supabaseStorageBaseUrl;

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    @Value("${supabase.storage.bucket:brewquery_profile_picture}")
    private String storageBucket;

    // ── GET /user/{userId}/profile ────────────────────────────────────────────

    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getProfile(Integer userId) {
        try {
            UserRawProfileDBO raw = profileRepo.findProfileById(userId);
            if (raw == null) {
                return ApiResponse.call(HttpStatus.NOT_FOUND, MessageConstants.USER_NOT_FOUND);
            }

            List<JudgeResultDTO> allResults = fetchAllResults(userId);

            List<JudgeResultDTO> passed = filterByVerdict(allResults, "PASS");

            Set<String> solvedQIds = extractQuestionIds(passed);
            int questionsSolved = solvedQIds.size();

            int datasetsCompleted = 0;
            if (!solvedQIds.isEmpty()) {
                datasetsCompleted = (int) questionRepository.findAllById(solvedQIds)
                        .stream()
                        .map(Question::getDatasetId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count();
            }

            int streak = computeStreak(extractDates(allResults));

            LevelDTO level = LevelUtils.compute(allResults.size());

            UserProfileResponseDTO dto = new UserProfileResponseDTO();
            dto.setUserId(raw.getUserId());
            dto.setName(raw.getFirstName() + " " + raw.getLastName());
            dto.setRole(toDisplayRole(raw.getRole()));
            dto.setEmail(raw.getEmail());
            dto.setPhone(buildPhone(raw.getCountryCode(), raw.getPhoneNumber()));
            dto.setJoinedAt(formatJoinedAt(raw.getCreatedAt()));
            dto.setLocation(raw.getLocation());
            dto.setBio(raw.getBio());
            dto.setAvatar(raw.getProfilePictureUrl());
            dto.setLevel(level);
            dto.setStats(new ProfileStatsDTO(questionsSolved, streak, datasetsCompleted));

            return ApiResponse.call(HttpStatus.OK, MessageConstants.PROFILE_FETCHED, dto);

        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── GET /user/{userId}/stats?period=all|year|month ────────────────────────

    public ResponseEntity<ApiResponse<UserStatsResponseDTO>> getStats(Integer userId, String period) {
        try {
            List<JudgeResultDTO> results = filterByPeriod(fetchAllResults(userId), period);

            int total   = results.size();
            int correct = filterByVerdict(results, "PASS").size();
            int wrong   = filterByVerdict(results, "FAIL").size();

            OptionalDouble avgMsOpt = results.stream()
                    .mapToLong(UserService::execMs)
                    .filter(ms -> ms > 0)
                    .average();

            int    correctPct = total > 0 ? (int) Math.round(correct * 100.0 / total) : 0;
            int    wrongPct   = total > 0 ? (int) Math.round(wrong   * 100.0 / total) : 0;
            String avgTime    = avgMsOpt.isPresent() ? formatAvgTime((long) avgMsOpt.getAsDouble()) : "00:00:00";

            Set<String> allQIds = extractQuestionIds(results);

            Map<String, String> qDiffMap    = new HashMap<>();
            Map<String, String> qDatasetMap = new HashMap<>();
            if (!allQIds.isEmpty()) {
                questionRepository.findAllById(allQIds).forEach(q -> {
                    qDiffMap.put(q.getId(),
                            q.getDifficulty() != null ? q.getDifficulty().toLowerCase() : "easy");
                    qDatasetMap.put(q.getId(), q.getDatasetId());
                });
            }

            Map<String, Integer> diffCount = new HashMap<>();
            diffCount.put("easy", 0); diffCount.put("medium", 0); diffCount.put("hard", 0);
            filterByVerdict(results, "PASS").forEach(r -> {
                String qId  = r.getQuestionId();
                String diff = qId != null ? qDiffMap.getOrDefault(qId, "easy") : "easy";
                diffCount.merge(diff, 1, Integer::sum);
            });
            DifficultyDTO difficulty = new DifficultyDTO(
                    diffCount.get("easy"), diffCount.get("medium"), diffCount.get("hard"));

            Map<String, Integer> datasetCount = new HashMap<>();
            filterByVerdict(results, "PASS").forEach(r -> {
                String qId  = r.getQuestionId();
                String dsId = qId != null ? qDatasetMap.get(qId) : null;
                if (dsId != null) datasetCount.merge(dsId, 1, Integer::sum);
            });

            List<FavouriteDatasetDTO> favouriteDatasets = new ArrayList<>();
            if (!datasetCount.isEmpty()) {
                List<String> topIds = datasetCount.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .limit(3)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                Map<String, String> dsNames = new HashMap<>();
                datasetRepository.findAllById(topIds)
                        .forEach(d -> dsNames.put(d.getId(), d.getTitle()));

                topIds.forEach(id -> favouriteDatasets.add(
                        new FavouriteDatasetDTO(dsNames.getOrDefault(id, id), datasetCount.get(id))));
            }

            List<BadgeDTO> badges = profileRepo.findBadgesByUserId(userId);

            UserStatsResponseDTO dto = new UserStatsResponseDTO();
            dto.setTotalSubmissions(total);
            dto.setCorrectSubmissions(correct);
            dto.setCorrectPct(correctPct);
            dto.setWrongSubmissions(wrong);
            dto.setWrongPct(wrongPct);
            dto.setAvgTime(avgTime);
            dto.setDifficulty(difficulty);
            dto.setFavouriteDatasets(favouriteDatasets);
            dto.setRecentBadges(badges);

            return ApiResponse.call(HttpStatus.OK, MessageConstants.STATS_FETCHED, dto);

        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── GET /user/{userId}/submissions?limit=5 ────────────────────────────────

    public ResponseEntity<ApiResponse<List<UserSubmissionDTO>>> getSubmissions(Integer userId, int limit) {
        try {
            List<JudgeResultDTO> results = fetchAllResults(userId);
            results.sort((a, b) -> {
                LocalDateTime tsA = timestamp(a);
                LocalDateTime tsB = timestamp(b);
                if (tsA == null && tsB == null) return 0;
                if (tsA == null) return 1;
                if (tsB == null) return -1;
                return tsB.compareTo(tsA);
            });
            if (results.size() > limit) results = results.subList(0, limit);

            Set<String> qIds = extractQuestionIds(results);
            Map<String, Question> questionMap = new HashMap<>();
            Set<String> dsIds = new HashSet<>();

            if (!qIds.isEmpty()) {
                questionRepository.findAllById(qIds).forEach(q -> {
                    questionMap.put(q.getId(), q);
                    if (q.getDatasetId() != null) dsIds.add(q.getDatasetId());
                });
            }

            Map<String, String> datasetNameMap = new HashMap<>();
            if (!dsIds.isEmpty()) {
                datasetRepository.findAllById(dsIds)
                        .forEach(d -> datasetNameMap.put(d.getId(), d.getTitle()));
            }

            List<UserSubmissionDTO> submissions = results.stream().map(r -> {
                String qId           = r.getQuestionId();
                Question q           = questionMap.get(qId);
                String overallStatus = overallStatus(r);

                String datasetName = null;
                String diff        = null;
                if (q != null) {
                    diff       = q.getDifficulty();
                    datasetName = datasetNameMap.get(q.getDatasetId());
                }

                UserSubmissionDTO sub = new UserSubmissionDTO();
                sub.setId(r.getJobId());
                sub.setQuestionTitle(q != null ? q.getTitle() : (qId != null ? qId : "—"));
                sub.setDataset(datasetName != null ? datasetName : "Unknown");
                sub.setLevel(diff != null ? capitalize(diff) : "—");
                sub.setLanguage("SQL");
                sub.setTimeTaken(formatTimeTaken(execMs(r)));
                sub.setSubmittedAt(timestamp(r) != null ? formatSubmittedAt(timestamp(r)) : "—");
                sub.setResult("PASS".equals(overallStatus) ? "Accepted" : "Wrong Answer");
                return sub;
            }).collect(Collectors.toList());

            return ApiResponse.call(HttpStatus.OK, MessageConstants.SUBMISSIONS_FETCHED, submissions);

        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── GET /user/{userId}/heatmap ────────────────────────────────────────────

    public ResponseEntity<ApiResponse<List<HeatmapEntryDTO>>> getHeatmap(Integer userId) {
        try {
            List<HeatmapEntryDTO> heatmap = fetchAllResults(userId).stream()
                    .map(UserService::timestamp)
                    .filter(Objects::nonNull)
                    .map(ts -> ts.toLocalDate().toString())
                    .collect(Collectors.groupingBy(d -> d, Collectors.counting()))
                    .entrySet().stream()
                    .map(e -> new HeatmapEntryDTO(e.getKey(), e.getValue().intValue()))
                    .sorted(Comparator.comparing(HeatmapEntryDTO::getDate))
                    .collect(Collectors.toList());

            return ApiResponse.call(HttpStatus.OK, MessageConstants.HEATMAP_FETCHED, heatmap);

        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── PUT /user/{userId}/avatar ─────────────────────────────────────────────

    public ResponseEntity<ApiResponse<String>> uploadAvatar(Integer userId, MultipartFile file) {
        try {
            if (file == null || file.isEmpty())
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.AVATAR_EMPTY);
            if (file.getSize() > 2 * 1024 * 1024)
                return ApiResponse.call(HttpStatus.BAD_REQUEST, MessageConstants.AVATAR_TOO_LARGE);

            String base = supabaseStorageBaseUrl.replaceAll("/+$", "");
            String storageUrl = base + "/object/" + storageBucket + "/" + userId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseServiceRoleKey);
            headers.setContentType(MediaType.parseMediaType(
                    file.getContentType() != null ? file.getContentType() : "application/octet-stream"));
            headers.set("x-upsert", "true");

            HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
            restTemplate.postForEntity(storageUrl, entity, String.class);

            String publicUrl = base + "/object/public/" + storageBucket + "/" + userId;

            profileRepo.updateProfilePictureUrl(userId, publicUrl);

            return ApiResponse.call(HttpStatus.OK, MessageConstants.AVATAR_UPLOADED, publicUrl);

        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.INTERNAL_SERVER_ERROR, e);
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private List<JudgeResultDTO> fetchAllResults(Integer userId) {
        return judgeResultRepo.findByUserId(String.valueOf(userId));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> toResultMap(JudgeResultDTO r) {
        if (r.getResult() instanceof Map) return (Map<String, Object>) r.getResult();
        return Collections.emptyMap();
    }

    private static String overallStatus(JudgeResultDTO r) {
        Object status = toResultMap(r).get("overallStatus");
        return status != null ? status.toString() : null;
    }

    private static long execMs(JudgeResultDTO r) {
        Object ms = toResultMap(r).get("totalExecutionMs");
        return ms instanceof Number ? ((Number) ms).longValue() : 0L;
    }

    private static LocalDateTime timestamp(JudgeResultDTO r) {
        if (r.getSubmittedAt() != null) {
            return r.getSubmittedAt().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();
        }
        return null;
    }

    private static List<JudgeResultDTO> filterByVerdict(List<JudgeResultDTO> list, String verdict) {
        return list.stream()
                .filter(r -> verdict.equals(overallStatus(r)))
                .collect(Collectors.toList());
    }

    private static Set<String> extractQuestionIds(List<JudgeResultDTO> list) {
        return list.stream()
                .map(JudgeResultDTO::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private static List<LocalDate> extractDates(List<JudgeResultDTO> results) {
        return results.stream()
                .map(UserService::timestamp)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    private static int computeStreak(List<LocalDate> sortedDesc) {
        if (sortedDesc.isEmpty()) return 0;
        LocalDate expected = LocalDate.now();
        if (!sortedDesc.get(0).equals(expected)) expected = expected.minusDays(1);
        int streak = 0;
        for (LocalDate d : sortedDesc) {
            if (d.equals(expected)) { streak++; expected = expected.minusDays(1); }
            else if (d.isBefore(expected)) break;
        }
        return streak;
    }

    private static List<JudgeResultDTO> filterByPeriod(List<JudgeResultDTO> list, String period) {
        if (period == null || "all".equals(period)) return list;
        LocalDateTime cutoff = "year".equals(period)
                ? LocalDate.now().withDayOfYear(1).atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return list.stream()
                .filter(r -> { LocalDateTime ts = timestamp(r); return ts != null && !ts.isBefore(cutoff); })
                .collect(Collectors.toList());
    }

    // ── Formatters ────────────────────────────────────────────────────────────

    private static String formatJoinedAt(java.sql.Timestamp ts) {
        if (ts == null) return null;
        return ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("MMM yyyy"));
    }

    private static String formatTimeTaken(long ms) {
        long totalSecs = ms / 1000;
        long mins = totalSecs / 60;
        long secs = totalSecs % 60;
        return mins > 0 ? mins + "m " + secs + "s" : secs + "s";
    }

    private static String formatAvgTime(long ms) {
        long s = ms / 1000;
        return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }

    private static String formatSubmittedAt(LocalDateTime ldt) {
        return ldt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    private static String buildPhone(String countryCode, String phone) {
        if (phone == null) return null;
        return countryCode != null ? countryCode + " " + phone : phone;
    }

    private static String toDisplayRole(String roleName) {
        if (roleName == null) return "Learner";
        return switch (roleName.toUpperCase()) {
            case "SUPERADMIN" -> "Super Admin";
            case "ADMIN"      -> "Admin";
            default           -> "Learner";
        };
    }
}
