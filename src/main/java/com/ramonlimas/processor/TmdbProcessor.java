package com.ramonlimas.processor;

import com.ramonlimas.client.TmdbClient;
import com.ramonlimas.domain.model.DataLake;
import com.ramonlimas.infrastructure.persistence.DataLakeRepository;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class TmdbProcessor {

    @Inject
    private TmdbClient tmdbClient;

    @Inject
    private DataLakeRepository dataLakeRepository;

    @Value("${tmdb.api.key}")
    private String apiKey;

    public Optional<DataLake> fetchAndSave(String name, String type) {
        AtomicReference<String> nameRef = new AtomicReference<>(name);

        DataLake existing = alreadyExists(name);
        if (existing != null) return Optional.of(existing);

        List<Map<String, Object>> results = searchWithFallback(nameRef, type);
        if (results.isEmpty()) return Optional.empty();

        Map<String, Object> closestMatch = getClosestMatch(results, nameRef.get());
        if (closestMatch == null) return Optional.empty();

        closestMatch.put("original_query", name); // apenas informativo
        return Optional.of(saveToDataLake(closestMatch, type));
    }

    private DataLake alreadyExists(String name) {
        Optional<DataLake> optional = dataLakeRepository.findByTitle(name);

        if (optional.isPresent()) {
            DataLake datalake = optional.get();
            if (datalake.getUrlPoster() != null) {
                log.warn("Título {} já existe, ignorando chamada", datalake.getTitle());
                return datalake;
            }

            dataLakeRepository.deleteById(datalake.getId());
            return null;
        }

        return null;
    }

    private List<Map<String, Object>> searchWithFallback(AtomicReference<String> name, String type) {
        List<Map<String, Object>> results = search(name.get(), type);

        if (results.isEmpty()) {
            String fallbackType = "movie".equals(type) ? "tv" : "movie";
            log.warn("Nada encontrado para {} como {}, tentando como {}", name.get(), type, fallbackType);
            results = search(name.get(), fallbackType);
        }

        if (results.isEmpty()) {
            String[] parts = name.get().trim().split("\\s+");
            String reduced = parts.length >= 2 ? parts[0] + " " + parts[1] : parts[0];

            log.warn("Pesquisando {} com nome reduzido: {}", type, reduced);
            results = search(reduced, type);
            if (!results.isEmpty()) {
                name.set(reduced);
            }
        }

        return results;
    }

    private List<Map<String, Object>> search(String name, String type) {
        try {
            Map<String, Object> response = "movie".equals(type)
                    ? tmdbClient.searchMovie(name, apiKey)
                    : tmdbClient.searchTv(name, apiKey);

            return (List<Map<String, Object>>) response.getOrDefault("results", Collections.emptyList());
        } catch (HttpClientResponseException e) {
            log.error("Erro na busca do TMDB: {}", e.getResponse().body());
            return Collections.emptyList();
        }
    }

    private Map<String, Object> getClosestMatch(List<Map<String, Object>> results, String name) {
        return results.stream()
                .max(Comparator.comparingInt(r -> calculateSimilarityScore(
                        (String) Optional.ofNullable(r.get("title")).orElse(r.get("name")), name)))
                .orElse(null);
    }

    private int calculateSimilarityScore(Object titleObj, String target) {
        if (titleObj == null) return 0;
        String title = titleObj.toString();
        int score = 0;
        for (int i = 0; i < Math.min(title.length(), target.length()); i++) {
            if (title.charAt(i) == target.charAt(i)) {
                score++;
            }
        }
        return score;
    }

    private DataLake saveToDataLake(Map<String, Object> match, String type) {
        DataLake dataLake = new DataLake();
        dataLake.setTitle((String) Optional.ofNullable(match.get("title")).orElse(match.get("name")));
        dataLake.setUrlPoster(match.get("poster_path") != null
                ? "https://image.tmdb.org/t/p/w500" + match.get("poster_path")
                : null);

        String date = (String) Optional.ofNullable(match.get("release_date"))
                .orElse(match.get("first_air_date"));

        dataLake.setReleaseYear(date != null && !date.isEmpty() ? date.split("-")[0] : null);

        // --- captura e resolve os gêneros ---
        @SuppressWarnings("unchecked")
        List<Integer> genreIds = (List<Integer>) match.get("genre_ids");
        String genres = resolveGenres(genreIds, type);
        dataLake.setGenre(genres);

        log.info("Título '{}' salvo com sucesso com gêneros: {}", dataLake.getTitle(), genres);
        return dataLakeRepository.save(dataLake);
    }

    private String resolveGenres(List<Integer> ids, String type) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        try {
            // busca lista completa de gêneros de acordo com o tipo
            Map<String, Object> resp = "movie".equals(type)
                    ? tmdbClient.getMovieGenres(apiKey)
                    : tmdbClient.getTvGenres(apiKey);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) resp.get("genres");

            return list.stream()
                    .filter(g -> ids.contains((Integer) g.get("id")))
                    .map(g -> (String) g.get("name"))
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            log.warn("Falha ao carregar gêneros da TMDB: {}", e.getMessage());
            return null;
        }
    }


}
