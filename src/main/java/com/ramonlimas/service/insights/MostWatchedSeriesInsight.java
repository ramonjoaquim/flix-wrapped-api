package com.ramonlimas.service.insights;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.ramonlimas.domain.dto.HistoryProcessedDTO;
import com.ramonlimas.domain.dto.InsightResultDTO;
import com.ramonlimas.domain.enums.InsightType;
import com.ramonlimas.domain.model.DataLake;
import com.ramonlimas.processor.TmdbProcessor;
import jakarta.inject.Singleton;
import org.bson.Document;

import java.util.*;

@Singleton
public class MostWatchedSeriesInsight implements Insight {

    private final MongoCollection<Document> collection;
    private final TmdbProcessor tmdbProcessor;

    public MostWatchedSeriesInsight(MongoCollection<Document> historyProcessedRawCollection, TmdbProcessor tmdbProcessor) {
        this.collection = historyProcessedRawCollection;
        this.tmdbProcessor = tmdbProcessor;
    }

    @Override
    public InsightType getType() {
        return InsightType.MOST_WATCHED_SERIES;
    }

    @Override
    public InsightResultDTO process(String userId, boolean includeTitles) {
        // Agregação para contar episódios por série
        AggregateIterable<Document> aggregate = collection.aggregate(List.of(
                Aggregates.match(Filters.and(
                        Filters.eq("userId", userId),
                        Filters.eq("data.type", HistoryProcessedDTO.Type.SERIES.name())
                )),
                Aggregates.unwind("$data.episodesList"), // Desenrolar a lista de episódios
                Aggregates.group("$data.title", Accumulators.sum("episodesCount", 1)), // Contar episódios
                Aggregates.sort(Sorts.descending("episodesCount")), // Ordenar pela quantidade de episódios
                Aggregates.limit(5) // Limitar a 5 séries mais assistidas
        ));

        List<InsightResultDTO.SeriesInfo> topSeries = new ArrayList<>();

        for (Document result : aggregate) {
            String title = result.getString("_id");
            int episodesCount = result.getInteger("episodesCount");

            Optional<DataLake> dataLake = tmdbProcessor.fetchAndSave(title, "series");
            String urlPoster = dataLake.map(DataLake::getUrlPoster).orElse(null);

            topSeries.add(new InsightResultDTO.SeriesInfo(title, episodesCount, urlPoster));
        }

        if (topSeries.isEmpty()) {
            return new InsightResultDTO(InsightType.MOST_WATCHED_SERIES,
                    "Nenhuma série assistida encontrada",
                    Collections.emptyMap());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("topSeries", topSeries);

        // Retornar o insight com os dados
        return new InsightResultDTO(InsightType.MOST_WATCHED_SERIES,
                "Top 5 séries mais assistidas",
                data);
    }
}

