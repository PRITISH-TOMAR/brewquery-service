package in.brewquery_engine.sql_engine.session;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class SessionCleaner {
    
    private final SessionManager sessionManager;

    @Scheduled(fixedDelay = 600000) // every 10 mins
    public void cleanOldSessions(){
        // logic
    }
}
