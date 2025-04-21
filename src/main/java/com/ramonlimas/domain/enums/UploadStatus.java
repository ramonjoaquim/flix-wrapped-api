package com.ramonlimas.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public enum UploadStatus {

    IN_PROGRESS("P", "Em progresso"),
    COMPLETED("C", "Concluído"),
    ERROR("E", "Erro");

    private final String code;
    private final String description;

    UploadStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static UploadStatus fromCode(String code) {
        for (UploadStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Código inválido: " + code);
    }
}