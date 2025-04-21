package com.ramonlimas.domain.model;

import com.ramonlimas.domain.dto.HistoryProcessedDTO;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
@Introspected
@MappedEntity("history_processed")
public class HistoryProcessedEntity {

    @Id
    private ObjectId id;
    private String userId;
    private HistoryProcessedDTO data;
    private Date createdAt;

    public HistoryProcessedEntity() {
        this.createdAt = new Date();
    }

}