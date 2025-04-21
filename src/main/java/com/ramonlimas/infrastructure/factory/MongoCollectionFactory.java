package com.ramonlimas.infrastructure.factory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.ramonlimas.domain.model.HistoryProcessedEntity;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.bson.Document;

@Factory
public class MongoCollectionFactory {

    private final String databaseName;
    private final String historyProcessedCollectionName;

    public MongoCollectionFactory(
            @Value("${mongodb.database}") String databaseName,
            @Value("${mongodb.collections.history-processed}") String historyProcessedCollectionName
    ) {
        this.databaseName = databaseName;
        this.historyProcessedCollectionName = historyProcessedCollectionName;
    }

    @Singleton
    public MongoCollection<HistoryProcessedEntity> historyProcessedEntityCollection(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        return database.getCollection(historyProcessedCollectionName, HistoryProcessedEntity.class);
    }

    @Singleton
    public MongoCollection<Document> historyProcessedRawCollection(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        return database.getCollection(historyProcessedCollectionName);
    }
}
