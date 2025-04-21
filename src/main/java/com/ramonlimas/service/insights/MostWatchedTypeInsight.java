package com.ramonlimas.service.insights;

import com.mongodb.client.MongoCollection;
import com.ramonlimas.domain.dto.HistoryProcessedDTO;
import com.ramonlimas.domain.dto.InsightResultDTO;
import com.ramonlimas.domain.enums.InsightType;
import com.ramonlimas.domain.model.HistoryProcessedEntity;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

@Singleton
public class MostWatchedTypeInsight implements Insight {

    private final MongoCollection<HistoryProcessedEntity> collection;

    public MostWatchedTypeInsight(MongoCollection<HistoryProcessedEntity> collection) {
        this.collection = collection;
    }

    @Override
    public InsightType getType() {
        return InsightType.MOST_WATCHED_TYPE;
    }

    @Override
    public InsightResultDTO process(String userId, boolean includeTitles) {
        List<HistoryProcessedEntity> all = collection.find(eq("userId", userId)).into(new ArrayList<>());

        long movies = all.stream()
                .filter(e -> e.getData().getType() == HistoryProcessedDTO.Type.MOVIE)
                .count();

        long series = all.stream()
                .filter(e -> e.getData().getType() == HistoryProcessedDTO.Type.SERIES)
                .count();

        String message = movies > series
                ? "Você assiste mais filmes 🎥 do que séries 📺!"
                : "Você assiste mais séries 📺 do que filmes 🎥!";

        return new InsightResultDTO(getType(), message, Map.of("movies", movies, "series", series));
    }
}