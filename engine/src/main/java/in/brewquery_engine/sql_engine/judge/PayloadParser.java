package in.brewquery_engine.sql_engine.judge;

import org.springframework.stereotype.Component;

import in.brewquery_engine.entities.judge.SQLPayload;
import tools.jackson.databind.ObjectMapper;

@Component
public class PayloadParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public SQLPayload parse(String rawPayload) throws Exception {
        return mapper.readValue(rawPayload, SQLPayload.class);
    }
}
