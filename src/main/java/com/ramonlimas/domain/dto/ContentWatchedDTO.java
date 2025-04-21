package com.ramonlimas.domain.dto;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@Serdeable
public class ContentWatchedDTO {
    private String title;
    private String type;
    private String dateWatched;
    private String urlPoster;

    public ContentWatchedDTO(String title, String type, Date dateWatched, String urlPoster) {
        this.title = title;
        this.type = type;
        this.dateWatched = dateWatched != null ? new SimpleDateFormat("dd/MM/yyyy").format(dateWatched) : null;
        this.urlPoster = urlPoster;
    }
}
