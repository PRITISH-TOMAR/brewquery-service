package in.brewquery_engine.sql_engine.judge;

import java.time.Duration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import in.brewquery_engine.entities.judge.JudgeJobPayload;
import in.brewquery_engine.entities.judge.RunTestcaseResponseDTO;
import in.brewquery_engine.service.JobsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobWorker implements CommandLineRunner {

    private final StringRedisTemplate redisTemplate;
    private final JobsService jobsService;
    private final ObjectMapper objectMapper;

    private static final String QUEUE_KEY    = "judge:queue:sql";
    private static final String RESULT_PREFIX = "result:sql:";
    private static final long   RESULT_TTL_S  = 3600;

    @Override
    public void run(String... args) {
        Thread worker = new Thread(this::workerLoop, "judge-worker");
        worker.setDaemon(true);
        worker.start();
        log.info("Judge worker started, listening on queue: {}", QUEUE_KEY);
    }

    private void workerLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Blocking pop — waits up to 5 s then loops (allows clean shutdown)
                String raw = redisTemplate.opsForList()
                        .rightPop(QUEUE_KEY, Duration.ofSeconds(5));
                if (raw == null) continue;

                JudgeJobPayload payload = objectMapper.readValue(raw, JudgeJobPayload.class);
                log.info("Processing job: {}", payload.getJobId());

                RunTestcaseResponseDTO result = jobsService.processSubmission(payload);

                String resultJson = objectMapper.writeValueAsString(result);
                redisTemplate.opsForValue().set(
                        RESULT_PREFIX + payload.getJobId(),
                        resultJson,
                        Duration.ofSeconds(RESULT_TTL_S));

                log.info("Job {} done — {}", payload.getJobId(), result.getOverallStatus());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Job processing error: {}", e.getMessage(), e);
            }
        }
    }
}
