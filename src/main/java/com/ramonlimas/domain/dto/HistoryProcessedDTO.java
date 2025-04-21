package com.ramonlimas.domain.dto;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Serdeable
public class HistoryProcessedDTO {

    private String title;
    private Date dateWatch;
    private Type type;
    private List<Episodes> episodesList;

    public enum Type {
        MOVIE,
        SERIES
    }

    @Getter
    @Setter
    @Serdeable
    public static class Episodes {
        private String title;
        private Date dateWatched;
    }
}
