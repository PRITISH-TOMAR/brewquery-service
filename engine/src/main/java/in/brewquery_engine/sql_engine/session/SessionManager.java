package in.brewquery_engine.sql_engine.session;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class SessionManager {
    
    private final ConcurrentHashMap<String, DatasetSession> sessions  = new ConcurrentHashMap<>();

    public void saveSession(DatasetSession session){
        sessions.put(session.getSessionId(), session);
    }

    public DatasetSession getSession(String sessionId){
        return sessions.get(sessionId);
    }

    public void removeSession(String sessionId){
        sessions.remove(sessionId);
    }
}
