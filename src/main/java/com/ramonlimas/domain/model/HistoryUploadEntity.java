package com.ramonlimas.domain.model;

import com.ramonlimas.domain.enums.UploadStatus;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
@Introspected
@Serdeable
@MappedEntity("history_upload")
public class HistoryUploadEntity {

    @Id
    private ObjectId id;
    private UploadStatus status;
    private String userId;
    private String error;

    @BsonProperty("createdAt")
    private Date createdAt;

    @BsonProperty("concludeAt")
    private Date concludeAt;

    public HistoryUploadEntity() {}

}