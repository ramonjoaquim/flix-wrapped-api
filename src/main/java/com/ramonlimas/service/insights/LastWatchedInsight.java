package com.ramonlimas.service.insights;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.ramonlimas.domain.dto.ContentWatchedDTO;
import com.ramonlimas.domain.dto.InsightResultDTO;
import com.ramonlimas.domain.enums.InsightType;
import com.ramonlimas.domain.model.DataLake;
import com.ramonlimas.processor.TmdbProcessor;
import jakarta.inject.Singleton;
import org.bson.Document;

import java.util.*;

@Singleton
public class LastWatchedInsight implements Insight {

    private final MongoCollection<Document> historyProcessedRawCollection;
    private final TmdbProcessor tmdbProcessor;

    public LastWatchedInsight(MongoCollection<Document> historyProcessedRawCollection, TmdbProcessor tmdbProcessor) {
        this.historyProcessedRawCollection = historyProcessedRawCollection;
        this.tmdbProcessor = tmdbProcessor;
    }

    @Override
    public InsightType getType() {
        return InsightType.LAST_WATCHED;
    }

    @Override
    public InsightResultDTO process(String userId, boolean includeTitles) {
        // Realiza o aggregate para pegar os 5 últimos conteúdos assistidos
        AggregateIterable<Document> aggregate = historyProcessedRawCollection.aggregate(List.of(
                Aggregates.match(Filters.eq("userId", userId)),
                Aggregates.sort(Sorts.descending("data.dateWatch")), // Ordena pela data de visualização
                Aggregates.limit(5) // Limita para pegar as 5 últimas assistidas
        ));

        List<ContentWatchedDTO> contentDetails = new ArrayList<>(); // Lista de ContentWatchedDTO

        // Itera sobre os resultados e extrai os títulos, tipos e datas
        for (Document result : aggregate) {
            Document data = (Document) result.get("data"); // Acessa o campo 'data' que é um Document
            String title = data.getString("title");
            String type = data.getString("type");
            Date dateWatch = data.getDate("dateWatch");

            // Cria o ContentWatchedDTO para cada conteúdo assistido
            Optional<DataLake> dataLake = tmdbProcessor.fetchAndSave(title, type);
            String urlPoster = dataLake.map(DataLake::getUrlPoster).orElse(null);
            contentDetails.add(new ContentWatchedDTO(title, type, dateWatch, urlPoster));
        }

        if (!contentDetails.isEmpty()) {
            // Cria uma mensagem amigável, se houver conteúdo
            String message = "Aqui estão os últimos conteúdos que você assistiu:";

            // Retorna o DTO com as informações formatadas
            return new InsightResultDTO(
                    getType(),
                    message,
                    Map.of("contentDetails", contentDetails)
            );
        }

        return null; // Caso não haja conteúdos assistidos
    }
}