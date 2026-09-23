package club.sqlhub.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import club.sqlhub.Repository.UserProfileRepository;
import club.sqlhub.constants.MessageConstants;
import club.sqlhub.entity.user.DBO.UserRawProfileDBO;
import club.sqlhub.entity.user.DTO.profile.*;
import club.sqlhub.mongo.models.Dataset;
import club.sqlhub.mongo.models.JudgeResult.JudgeResultDTO;
import club.sqlhub.mongo.models.Question;
import club.sqlhub.mongo.repository.DatasetRepository;
import club.sqlhub.mongo.repository.QuestionRepository;
import club.sqlhub.utils.APiResponse.ApiResponse;
import club.sqlhub.utils.User.LevelUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository profileRepo;
    private final MongoTemplate         mongoTemplate;
    private final QuestionRepository    questionRepository;
    private final DatasetRepository     datasetRepository;
    private final RestTemplate          restTemplate;

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

            // Submissions with PASS verdict
            List<JudgeResultDTO> passed = filterByVerdict(allResults, "PASS");

            // questionsSolved = distinct questionIds with PASS
            Set<String> solvedQIds = extractQuestionIds(passed);
            int questionsSolved = solvedQIds.size();

            // datasetsCompleted = distinct datasetIds the user has solved a question in
            int datasetsCompleted = 0;
            if (!solvedQIds.isEmpty()) {
                datasetsCompleted = (int) questionRepository.findAllById(solvedQIds)
                        .stream()
                        .map(Question::getDatasetId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count();
            }

            // dayStreak — consecutive days with any submission ending today
            int streak = computeStreak(extractDates(allResults));

            LevelDTO level = LevelUtils.compute(allResults.size());

            UserProfileResponseDTO dto = new UserProfileResponseDTO();
            dto.setUserId(raw.getUserId());
            dto.setName(raw.getFirstName() + " " + raw.getLastName());
            dto.setRole(toDisplayRole(raw.getRoleName()));
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
            int correct = (int) filterByVerdict(results, "PASS").stream().count();
            int wrong   = (int) filterByVerdict(results, "FAIL").stream().count();

            OptionalDouble avgMsOpt = results.stream()
                    .mapToLong(r -> execMs(r))
                    .filter(ms -> ms > 0)
                    .average();

            int    correctPct = total > 0 ? (int) Math.round(correct * 100.0 / total) : 0;
            int    wrongPct   = total > 0 ? (int) Math.round(wrong   * 100.0 / total) : 0;
            String avgTime    = avgMsOpt.isPresent() ? formatAvgTime((long) avgMsOpt.getAsDouble()) : "00:00:00";

            // Collect all unique questionIds across all submissions
            Set<String> allQIds = extractQuestionIds(results);

            // Single MongoDB call → build two lookup maps
            Map<String, String> qDiffMap    = new HashMap<>();  // questionId → difficulty
            Map<String, String> qDatasetMap = new HashMap<>();  // questionId → datasetId
            if (!allQIds.isEmpty()) {
                questionRepository.findAllById(allQIds).forEach(q -> {
                    qDiffMap.put(q.getId(),
                            q.getDifficulty() != null ? q.getDifficulty().toLowerCase() : "easy");
                    qDatasetMap.put(q.getId(), q.getDatasetId());
                });
            }

            // Difficulty counts — only correct submissions
            Map<String, Integer> diffCount = new HashMap<>();
            diffCount.put("easy", 0); diffCount.put("medium", 0); diffCount.put("hard", 0);
            filterByVerdict(results, "PASS").forEach(r -> {
                String qId  = questionId(r);
                String diff = qId != null ? qDiffMap.getOrDefault(qId, "easy") : "easy";
                diffCount.merge(diff, 1, Integer::sum);
            });
            DifficultyDTO difficulty = new DifficultyDTO(
                    diffCount.get("easy"), diffCount.get("medium"), diffCount.get("hard"));

            // Favourite datasets — ranked by number of correct submissions
            Map<String, Integer> datasetCount = new HashMap<>();
            filterByVerdict(results, "PASS").forEach(r -> {
                String qId  = questionId(r);
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
            // Fetch all, sort newest-first in Java (avoids fragile nested-field sort in Mongo)
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

            // Resolve questionIds → titles, difficulties, datasetIds
            Set<String> qIds = extractQuestionIds(results);
            Map<String, Question> questionMap = new HashMap<>();
            Set<String> dsIds = new HashSet<>();

            if (!qIds.isEmpty()) {
                questionRepository.findAllById(qIds).forEach(q -> {
                    questionMap.put(q.getId(), q);
                    if (q.getDatasetId() != null) dsIds.add(q.getDatasetId());
                });
            }

            // Resolve datasetIds → names
            Map<String, String> datasetNameMap = new HashMap<>();
            if (!dsIds.isEmpty()) {
                datasetRepository.findAllById(dsIds)
                        .forEach(d -> datasetNameMap.put(d.getId(), d.getTitle()));
            }

            List<UserSubmissionDTO> submissions = results.stream().map(r -> {
                Map<String, Object> rm  = toMap(r.getResult());
                String qId              = (String) rm.get("questionId");
                Question q              = questionMap.get(qId);
                String overallStatus    = (String) rm.get("overallStatus");

                String datasetName = null;
                String difficulty  = null;
                if (q != null) {
                    difficulty = q.getDifficulty();
                    datasetName = datasetNameMap.get(q.getDatasetId());
                }

                UserSubmissionDTO sub = new UserSubmissionDTO();
                sub.setId(r.getJobId());
                sub.setQuestionTitle(q != null ? q.getTitle() : (qId != null ? qId : "—"));
                sub.setDataset(datasetName != null ? datasetName : "Unknown");
                sub.setLevel(difficulty != null ? capitalize(difficulty) : "—");
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
                    .map(r -> timestamp(r))
                    .filter(Objects::nonNull)
                    .map(ts -> ts.toLocalDate().toString())          // "YYYY-MM-DD"
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

            String base = supabaseStorageBaseUrl.replaceAll("/+$", ""); // strip trailing slash
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
        Query q = new Query(Criteria.where("userId").is(String.valueOf(userId)));
        return mongoTemplate.find(q, JudgeResultDTO.class);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> toMap(Object result) {
        if (result instanceof Map) return (Map<String, Object>) result;
        return Collections.emptyMap();
    }

    private static String questionId(JudgeResultDTO r) {
        return (String) toMap(r.getResult()).get("questionId");
    }

    private static long execMs(JudgeResultDTO r) {
        Object ms = toMap(r.getResult()).get("totalExecutionMs");
        return ms instanceof Number ? ((Number) ms).longValue() : 0L;
    }

    private static LocalDateTime timestamp(JudgeResultDTO r) {
        return extractTimestamp(toMap(r.getResult()).get("timestamp"));
    }

    private static LocalDateTime extractTimestamp(Object ts) {
        if (ts == null) return null;
        if (ts instanceof java.util.Date)
            return ((java.util.Date) ts).toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();
        if (ts instanceof Long)
            return Instant.ofEpochMilli((Long) ts).atZone(ZoneId.of("UTC")).toLocalDateTime();
        if (ts instanceof Map) {
            Object d = ((Map<?, ?>) ts).get("$date");
            if (d instanceof String) {
                try { return Instant.parse((String) d).atZone(ZoneId.of("UTC")).toLocalDateTime(); }
                catch (Exception ignored) { return null; }
            }
        }
        try { return LocalDateTime.parse(ts.toString(), DateTimeFormatter.ISO_DATE_TIME); }
        catch (Exception ignored) { return null; }
    }

    private static List<JudgeResultDTO> filterByVerdict(List<JudgeResultDTO> list, String verdict) {
        return list.stream()
                .filter(r -> verdict.equals(toMap(r.getResult()).get("overallStatus")))
                .collect(Collectors.toList());
    }

    private static Set<String> extractQuestionIds(List<JudgeResultDTO> list) {
        return list.stream()
                .map(UserService::questionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private static List<LocalDate> extractDates(List<JudgeResultDTO> results) {
        return results.stream()
                .map(r -> timestamp(r))
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    /** Consecutive days ending today (or yesterday if no submission today). */
    private static int computeStreak(List<LocalDate> sortedDesc) {
        if (sortedDesc.isEmpty()) return 0;
        LocalDate expected = LocalDate.now();
        // Allow streak if last submission was yesterday
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
            case "ADMIN" -> "Admin";
            default      -> "Learner";
        };
    }
}
