package in.brewquery_engine.sql_engine.session;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import in.brewquery_engine.constants.StorageConstants;
import in.brewquery_engine.entities.DatasetSessionDTO;
import in.brewquery_engine.entities.SessionDTO;

@Component
public class SessionManager {

    @Autowired
    private StringRedisTemplate redis;

    public void saveSession(String sessionId, String dataSetId) {
        redis.opsForValue().set("session:" + sessionId, dataSetId,
                Duration.ofMinutes(StorageConstants.REDIS_TTL_SESSION));
    }

    public boolean hasSession(String sessionId) {
        return Boolean.TRUE.equals(redis.hasKey("session:" + sessionId));
    }

    public void deleteSession(String sessionId) {
        redis.delete("session:" + sessionId);
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
}
