package com.ramonlimas.domain.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Introspected
@MappedEntity("data_lake")
public class DataLake {

    @Id
    private ObjectId id;
    private String title;
    private String urlPoster;
    private String releaseYear;
    private String genre;
}