package com.ramonlimas.processor;

import com.ramonlimas.client.OmdbClient;
import com.ramonlimas.domain.model.DataLake;
import com.ramonlimas.infrastructure.persistence.DataLakeRepository;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Singleton
public class OmdbProcessor {

    @Inject
    private OmdbClient omdbClient;

    @Inject
    private DataLakeRepository dataLakeRepository;

    public Optional<DataLake> fetchAndSave(String name, String type) {
        AtomicReference<String> nameRef = new AtomicReference<>(name);

        DataLake dataLake = alreadyExists(name);
        if (dataLake != null) return Optional.of(dataLake);

        List<Map<String, Object>> results = searchWithFallback(nameRef, type);
        if (results.isEmpty()) return Optional.empty();

        Map<String, Object> closestMatch = getClosestMatch(results, nameRef.get());
        if (closestMatch == null) return Optional.empty();

        closestMatch.replace("Title", name);
        return Optional.of(saveToDataLake(closestMatch));
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
            String alternateType = "movie".equals(type) ? "series" : "movie";
            log.warn("Nenhum resultado para {} como {}, tentando como {}...", name.get(), type, alternateType);
            results = search(name.get(), alternateType);
        }

        if (results.isEmpty()) {
            log.warn("Nenhum resultado encontrado para {} em ambos os tipos", name.get());
        }

        if (results.isEmpty()) {
            String[] parts = name.get().trim().split("\\s+");
            String reduced = parts.length >= 2 ? parts[0] + " " + parts[1] : parts[0];

            log.warn("Pesquisando título {} com uma string menor para dar match", reduced);
            results = search(reduced, type);
            if (!results.isEmpty()) {
               name.set(reduced);
            }
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> search(String name, String type) {
        Map<String, Object> response = Collections.EMPTY_MAP;
        try {
            response = omdbClient.search(name, type);
        } catch (HttpClientResponseException e) {
            log.error("Erro ao realizar busca omdb: {}", e.getResponse().body().toString());
        }
        return (List<Map<String, Object>>) response.getOrDefault("Search", Collections.emptyList());
    }

    private Map<String, Object> getClosestMatch(List<Map<String, Object>> results, String name) {
        try {
            return results.stream()
                    .max(Comparator.comparingInt(r -> calculateSimilarityScore((String) r.get("Title"), name)))
                    .orElse(null);
        } catch (Exception e) {
            log.info("Erro ao encontrar o título mais próximo: {}", e.getMessage());
            return null;
        }
    }

    private int calculateSimilarityScore(String title, String target) {
        int score = 0;
        for (int i = 0; i < Math.min(title.length(), target.length()); i++) {
            if (title.charAt(i) == target.charAt(i)) {
                score++;
            }
        }
        return score;
    }

    private DataLake saveToDataLake(Map<String, Object> match) {
        DataLake dataLake = new DataLake();
        dataLake.setTitle((String) match.get("Title"));
        dataLake.setUrlPoster((String) match.get("Poster"));
        dataLake.setReleaseYear(extractStartYear((String) match.get("Year")));

        log.info("Título '{}' salvo com sucesso!", dataLake.getTitle());
        return dataLakeRepository.save(dataLake);
    }

    private String extractStartYear(String year) {
        if (year == null || year.isEmpty()) return null;
        return year.split("–")[0].trim();
    }
}
