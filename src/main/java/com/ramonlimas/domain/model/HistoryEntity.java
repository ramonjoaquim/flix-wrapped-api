package com.ramonlimas.domain.model;

import com.ramonlimas.domain.dto.HistoryCSV;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
@Introspected
@MappedEntity("history")
public class HistoryEntity {

    @Id
    private ObjectId id;
    private String userId;
    private String title;
    private Date date;
    private Date createdAt;

    public HistoryEntity() {}

    public HistoryEntity(String userId, String title, Date date) {
        this.userId = userId;
        this.title = title;
        this.date = date;
        this.createdAt = new Date();
    }

    public HistoryCSV toDto() {
        return new HistoryCSV(userId, title, date);
    }
}