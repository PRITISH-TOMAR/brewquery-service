package in.brewquery_engine.sql_engine.session;

import java.sql.Connection;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.brewquery_engine.constants.StorageConstants;
import in.brewquery_engine.entities.DatasetSessionDTO;

@Component
public class SessionManager {

    @Autowired
    private StringRedisTemplate redis;
    private final Map<String, Connection> activeSessions = new ConcurrentHashMap<>();

    public void saveSession(String sessionId, String dataSetId, Connection conn) {
        redis.opsForValue().set("session:" + sessionId, dataSetId,
                Duration.ofMinutes(StorageConstants.REDIS_TTL_SESSION));
        activeSessions.put(sessionId, conn);

    }

    public boolean hasSession(String sessionId) {
        return Boolean.TRUE.equals(redis.hasKey("session:" + sessionId));
    }

    public void deleteSession(String sessionId) {
        try {
            Connection conn = activeSessions.remove(sessionId);
            if (conn != null)
                conn.close();
        } catch (Exception ignored) {
        }
    }

    public boolean refreshSession(DatasetSessionDTO session) {
        Boolean exists = redis.hasKey("session:" + session.getSessionId());
        if (Boolean.TRUE.equals(exists)) {
            redis.expire("session:" + session.getSessionId(), Duration.ofMinutes(StorageConstants.REDIS_TTL_SESSION));
            session.setLastAccessTime(LocalDateTime.now());
            return true;
        }
        return false;
    }

    public Map<String, Connection> getActiveSessions() {
        return activeSessions;
    }

    @Scheduled(fixedRate = 3600000) // every 1 hour
    public void cleanupExpiredSessions() {
        for (String sessionId : getActiveSessions().keySet()) {
            if (!hasSession(sessionId)) {
                deleteSession(sessionId);
            }
        }
    }
}
