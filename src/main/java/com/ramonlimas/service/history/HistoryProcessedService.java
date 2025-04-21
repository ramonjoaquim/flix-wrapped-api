package com.ramonlimas.service.history;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.ramonlimas.domain.model.HistoryProcessedEntity;
import jakarta.inject.Singleton;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class HistoryProcessedService {

    private final MongoCollection<HistoryProcessedEntity> collection;

    public HistoryProcessedService(MongoCollection<HistoryProcessedEntity> collection) {
        this.collection = collection;
    }

    public List<HistoryProcessedEntity> findByUserIdAndTitleRegex(String userId, String filterTitle, int offset, int limit) {
        // Criar filtro
        Bson filter = Filters.and(
                Filters.eq("userId", userId),
                Filters.regex("data.title", ".*" + filterTitle.toLowerCase() + ".*", "i") // Filtro "contains"
        );

        // Criar ordenação
        Bson sort = Sorts.ascending("data.title");

        // Aplicar consulta com paginação e ordenação
        return collection.find(filter)
                .sort(sort)
                .skip(offset)
                .limit(limit)
                .into(new ArrayList<>());
    }

    public long countByUserIdAndTitleRegex(String userId, String filterTitle) {
        // Criar filtro de contagem
        Bson filter = Filters.and(
                Filters.eq("userId", userId),
                Filters.regex("data.title", ".*" + filterTitle + ".*", "i") // Filtro "contains"
        );

        return collection.countDocuments(filter);
    }
}
