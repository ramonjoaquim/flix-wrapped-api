package com.ramonlimas.domain.dto;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Serdeable
public class PaginatedResponse<T> {
    private List<T> data;
    private Long total;
    private int offset;
    private int limit;

    public PaginatedResponse(List<T> data, Long total, int offset, int limit) {
        this.data = data;
        this.total = total;
        this.offset = offset;
        this.limit = limit;
    }

}