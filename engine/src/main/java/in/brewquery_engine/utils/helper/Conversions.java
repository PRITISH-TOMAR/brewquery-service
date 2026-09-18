package in.brewquery_engine.utils.helper;

import java.time.LocalDate;
import java.time.LocalDateTime;

import in.brewquery_engine.entities.DatasetSessionDTO;
import in.brewquery_engine.entities.SessionDTO;

public class Conversions {

    public static DatasetSessionDTO SessionDTOtoDatasetSessionDTO(SessionDTO dto) {
        DatasetSessionDTO res = new DatasetSessionDTO();
        res.setDatasetId(dto.getDatasetId());
        res.setSessionId(dto.getSessionId());
        res.setSqlMode(dto.getSqlMode());

        return res;
    }
}
