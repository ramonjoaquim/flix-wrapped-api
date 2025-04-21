package com.ramonlimas.domain.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.Date;

@Serdeable
public record HistoryDTO(String title, Date date) {
}
