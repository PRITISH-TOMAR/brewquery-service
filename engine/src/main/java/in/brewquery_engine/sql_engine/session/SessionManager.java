package in.brewquery_engine.sql_engine.session;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import in.brewquery_engine.entities.DatasetSessionDTO;

@Component
public class SessionManager {
    
    private final ConcurrentHashMap<String, DatasetSessionDTO> sessions  = new ConcurrentHashMap<>();

    public void saveSession(DatasetSessionDTO session){
        sessions.put(session.getSessionId(), session);
    }

    public DatasetSessionDTO getSession(String sessionId){
        return sessions.get(sessionId);
    }

    public void removeSession(String sessionId){
        sessions.remove(sessionId);
    }
}
