package com.ramonlimas.domain.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record MetadadoDTO(String title, String urlPoster, String yearReleased) {
}
