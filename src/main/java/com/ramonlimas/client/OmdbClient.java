package com.ramonlimas.client;

import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import java.util.Map;

@Client("${omdb.url}") // URL configurada no application.yml
public interface OmdbClient {

    @Get("?apikey=${omdb.apikey}&s={name}&type={type}")
    Map<String, Object> search(@QueryValue String name, @QueryValue String type);
}