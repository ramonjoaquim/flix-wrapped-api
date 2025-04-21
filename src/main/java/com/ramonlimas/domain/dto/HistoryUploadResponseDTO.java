package com.ramonlimas.domain.dto;

import com.ramonlimas.domain.model.HistoryUploadEntity;
import io.micronaut.serde.annotation.Serdeable;

import java.util.Date;

@Serdeable
public record HistoryUploadResponseDTO(
        String id,
        String status,
        String userId,
        Date createdAt,
        Date concludeAt) {

    public HistoryUploadResponseDTO(HistoryUploadEntity entity) {
        this(
                entity.getId().toHexString(),
                entity.getStatus().name(),
                entity.getUserId(),
                entity.getCreatedAt(),
                entity.getConcludeAt()
        );
    }
}