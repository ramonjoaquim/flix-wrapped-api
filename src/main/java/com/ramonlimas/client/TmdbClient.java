package com.ramonlimas.client;

import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;

import java.util.Map;

@Client("${tmdb.url}")
public interface TmdbClient {

    @Get("/search/movie?query={title}&language=en-US&api_key={apiKey}")
    Map<String, Object> searchMovie(String title, @QueryValue("apiKey") String apiKey);

    @Get("/search/tv?query={title}&language=en-US&api_key={apiKey}")
    Map<String, Object> searchTv(String title, @QueryValue("apiKey") String apiKey);

    @Get("/genre/movie/list?language=en-US&api_key={apiKey}")
    Map<String, Object> getMovieGenres(@QueryValue("apiKey") String apiKey);

    @Get("/genre/tv/list?language=en-US&api_key={apiKey}")
    Map<String, Object> getTvGenres(@QueryValue("apiKey") String apiKey);
}