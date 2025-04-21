package com.ramonlimas.service.insights;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.ramonlimas.domain.dto.HistoryProcessedDTO;
import com.ramonlimas.domain.dto.InsightResultDTO;
import com.ramonlimas.domain.enums.InsightType;
import jakarta.inject.Singleton;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;

@Singleton
public class MostWatchedByYearInsight implements Insight {

    private final MongoCollection<Document> collection;

    public MostWatchedByYearInsight(MongoCollection<Document> historyProcessedRawCollection) {
        this.collection = historyProcessedRawCollection;
    }

    @Override
    public InsightType getType() {
        return InsightType.MOST_WATCHED_BY_YEAR;
    }

    @Override
    public InsightResultDTO process(String userId, boolean includeTitles) {
        // 1) pipeline de filmes: conta e (se solicitado) coleta títulos
        List<Bson> moviePipeline = new ArrayList<>(List.of(
                Aggregates.match(Filters.and(
                        Filters.eq("userId", userId),
                        Filters.eq("data.type", HistoryProcessedDTO.Type.MOVIE.name())
                )),
                Aggregates.group(
                        new Document("year", new Document("$year", "$data.dateWatch")),
                        Accumulators.sum("count", 1)
                ),
                Aggregates.sort(Sorts.ascending("_id.year"))
        ));
        if (includeTitles) {
            // inserir addToSet logo após o sum
            moviePipeline.set(1, Aggregates.group(
                    new Document("year", new Document("$year", "$data.dateWatch")),
                    Accumulators.sum("count", 1),
                    Accumulators.addToSet("movieTitles", "$data.title")
            ));
        }

        // 2) pipeline de séries: unwind, conta e (se solicitado) coleta títulos
        List<Bson> seriesPipeline = new ArrayList<>(List.of(
                Aggregates.match(Filters.and(
                        Filters.eq("userId", userId),
                        Filters.eq("data.type", HistoryProcessedDTO.Type.SERIES.name())
                )),
                Aggregates.unwind("$data.episodesList"),
                Aggregates.match(Filters.exists("data.episodesList.dateWatched", true)),
                Aggregates.group(
                        new Document("year", new Document("$year", "$data.episodesList.dateWatched")),
                        Accumulators.sum("count", 1)
                ),
                Aggregates.sort(Sorts.ascending("_id.year"))
        ));
        if (includeTitles) {
            seriesPipeline.set(3, Aggregates.group(
                    new Document("year", new Document("$year", "$data.episodesList.dateWatched")),
                    Accumulators.sum("count", 1),
                    Accumulators.addToSet("seriesTitles", "$data.episodesList.title") // Corrige coleta do título
            ));
        }

        AggregateIterable<Document> moviesAgg = collection.aggregate(moviePipeline);
        AggregateIterable<Document> seriesAgg = collection.aggregate(seriesPipeline);

        // 3) combinar resultados por ano
        Map<Integer, InsightResultDTO.YearlyContentConsumptionDTO> map = new TreeMap<>();
        for (Document doc : moviesAgg) {
            Document id = doc.get("_id", Document.class);
            Integer year = id.getInteger("year");
            int moviesCount = doc.getInteger("count", 0);
            InsightResultDTO.YearlyContentConsumptionDTO dto =
                    new InsightResultDTO.YearlyContentConsumptionDTO(year, moviesCount, 0);
            if (includeTitles) {
                @SuppressWarnings("unchecked")
                List<String> titles = (List<String>) doc.get("movieTitles");
                dto.setMoviesTitles(titles != null ? titles : Collections.emptyList());
            }
            map.put(year, dto);
        }

        for (Document doc : seriesAgg) {
            Document id = doc.get("_id", Document.class);
            Integer year = id.getInteger("year");
            int seriesCount = doc.getInteger("count", 0);

            // Recupera ou cria um DTO para aquele ano
            InsightResultDTO.YearlyContentConsumptionDTO dto =
                    map.getOrDefault(year, new InsightResultDTO.YearlyContentConsumptionDTO(year, 0, seriesCount));

            // Atualiza o contador e títulos de séries
            dto.setEpisodesCount(dto.getEpisodesCount() + seriesCount); // Corrige o contador somando ao existente
            if (includeTitles) {
                @SuppressWarnings("unchecked")
                List<String> titles = (List<String>) doc.get("seriesTitles");
                dto.setSeriesTitles(titles != null ? titles : Collections.emptyList());
            }
            map.put(year, dto);
        }

        List<InsightResultDTO.YearlyContentConsumptionDTO> yearlyData = new ArrayList<>(map.values());
        if (yearlyData.isEmpty()) {
            return new InsightResultDTO(
                    getType(),
                    "Nenhum conteúdo assistido encontrado",
                    Collections.emptyMap()
            );
        }

        Map<String, Object> data = Map.of("yearlyData", yearlyData);
        return new InsightResultDTO(
                getType(),
                "Resumo do consumo por ano",
                data
        );
    }
}