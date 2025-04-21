package com.ramonlimas.infrastructure.factory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import org.bson.Document;

@Singleton
public class MongoIndexInitializer {

    private final MongoClient mongoClient;
    private final String databaseName;

    public MongoIndexInitializer(MongoClient mongoClient, @Value("${mongodb.database}") String databaseName) {
        this.mongoClient = mongoClient;
        this.databaseName = databaseName;
    }

    @EventListener
    public void onStartup(StartupEvent event) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);

        MongoCollection<Document> dataLakeCollection = database.getCollection("data_lake");
        dataLakeCollection.createIndex(Indexes.ascending("title"));

        MongoCollection<Document> historyCollection = database.getCollection("history");
        historyCollection.createIndex(Indexes.ascending("userId"));

        MongoCollection<Document> historyProcessedCollection = database.getCollection("history_processed");
        historyProcessedCollection.createIndex(Indexes.compoundIndex(
                Indexes.ascending("userId"),
                Indexes.ascending("data.type")
        ));

        historyProcessedCollection.createIndex(Indexes.ascending("data.title"));

        historyProcessedCollection.createIndex(Indexes.ascending("data.episodesList"));
    }
}