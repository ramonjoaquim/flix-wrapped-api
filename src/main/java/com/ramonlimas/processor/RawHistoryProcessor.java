package com.ramonlimas.processor;

import com.ramonlimas.domain.dto.HistoryProcessedDTO;
import com.ramonlimas.domain.model.HistoryEntity;
import com.ramonlimas.domain.model.HistoryProcessedEntity;
import com.ramonlimas.infrastructure.persistence.HistoryProcessedRepository;
import com.ramonlimas.infrastructure.persistence.HistoryRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class RawHistoryProcessor {

    @Inject
    private HistoryRepository historyRepository;

    @Inject
    private HistoryProcessedRepository historyProcessedRepository;

    public void process(String userId) {
        log.info("Process started {}", userId);
        //deletar todos os dados processados para esse userId
        List<HistoryProcessedEntity> byUserId = historyProcessedRepository.findByUserId(userId);
        historyProcessedRepository.deleteAll(byUserId);

        List<HistoryEntity> rawHistory = historyRepository.findByUserId(userId);

        Map<String, List<HistoryEntity>> seriesMap = new HashMap<>();
        Map<String, HistoryEntity> movieMap = new HashMap<>();

        for (HistoryEntity history : rawHistory) {
            Optional<String> simplifiedTitle = simplifyTitle(history.getTitle());
            simplifiedTitle.ifPresent(title -> {
                if (isSeries(history.getTitle())) {
                    seriesMap.computeIfAbsent(title, k -> new ArrayList<>()).add(history);
                } else {
                    movieMap.merge(title, history, (existing, newEntry) ->
                            existing.getDate().after(newEntry.getDate()) ? existing : newEntry);
                }
            });
        }

        // Processar séries e consolidar episódios
        List<HistoryProcessedEntity> entitiesToSave = new ArrayList<>();
        for (Map.Entry<String, List<HistoryEntity>> entry : seriesMap.entrySet()) {
            HistoryProcessedDTO dto = new HistoryProcessedDTO();
            dto.setTitle(entry.getKey());

            // Ordenar episódios por título e transformar em DTO
            List<HistoryProcessedDTO.Episodes> episodesList = entry.getValue().stream()
                    .sorted(Comparator.comparingInt(this::extractSeason) // Ordenar pela temporada
                    .thenComparingInt(this::extractEpisode)) // Ordenar pelo episódio
                    .map(history -> {
                        HistoryProcessedDTO.Episodes episode = new HistoryProcessedDTO.Episodes();
                        episode.setTitle(history.getTitle());
                        episode.setDateWatched(history.getDate());
                        return episode;
                    })
                    .collect(Collectors.toList());

            dto.setEpisodesList(episodesList);
            dto.setType(HistoryProcessedDTO.Type.SERIES);
            HistoryProcessedEntity entity = new HistoryProcessedEntity();
            entity.setUserId(userId);
            entity.setData(dto);
            entitiesToSave.add(entity);
        }

        // Processar filmes
        for (Map.Entry<String, HistoryEntity> entry : movieMap.entrySet()) {
            HistoryProcessedDTO dto = new HistoryProcessedDTO();
            dto.setTitle(entry.getKey());
            dto.setDateWatch(entry.getValue().getDate());
            dto.setEpisodesList(Collections.emptyList());
            dto.setType(HistoryProcessedDTO.Type.MOVIE);

            HistoryProcessedEntity entity = new HistoryProcessedEntity();
            entity.setUserId(userId);
            entity.setData(dto);
            entitiesToSave.add(entity);
        }

        // Salvar no MongoDB
        historyProcessedRepository.saveAll(entitiesToSave);
        log.info("Process finished {}", userId);
    }

    private boolean isSeries(String title) {
        String lowerTitle = title.toLowerCase();
        return lowerTitle.contains("season") || lowerTitle.contains("temporada") ||
                lowerTitle.contains("episode") || lowerTitle.contains("episodio") ||
                lowerTitle.contains("chapter") || lowerTitle.contains("capitulo") ||
                lowerTitle.contains("limited series") || lowerTitle.contains("serie limitada") ||
                lowerTitle.contains("part") || lowerTitle.contains("parte");
    }

    private Optional<String> simplifyTitle(String title) {
        try {
            // Extrair o nome principal até os dois pontos ou o primeiro separador relevante
            String trim = title.split(":")[0].trim();
            return trim.isBlank() ? Optional.empty() : Optional.of(trim);
        } catch (Exception e) {
            return Optional.empty(); // Tratar exceção e retornar empty para títulos inválidos
        }
    }

    private int extractSeason(HistoryEntity history) {
        String title = history.getTitle().toLowerCase(); // Normalizando o título para evitar problemas de case
        try {
            // Procurar por padrões de "Season" ou "Temporada" e extrair o número
            Pattern pattern = Pattern.compile("(season|temporada)\\s*(\\d+)");
            Matcher matcher = pattern.matcher(title);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(2));
            }
        } catch (Exception e) {
            // Retorna 0 se não encontrar a temporada
        }
        return 0;
    }

    private int extractEpisode(HistoryEntity history) {
        String title = history.getTitle().toLowerCase(); // Normalizando o título
        try {
            // Procurar por padrões de "Episode", "Episodio", "Chapter", ou "Capitulo" e extrair o número
            Pattern pattern = Pattern.compile("(episode|episodio|chapter|capitulo)\\s*(\\d+)");
            Matcher matcher = pattern.matcher(title);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(2));
            }
        } catch (Exception e) {
            // Retorna 0 se não encontrar o episódio
        }
        return 0;
    }

}
