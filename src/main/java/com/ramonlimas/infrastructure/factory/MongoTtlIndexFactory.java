package com.ramonlimas.infrastructure.factory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;

import java.util.concurrent.TimeUnit;

@Singleton
public class MongoTtlIndexFactory {

    private final MongoClient mongoClient;
    private final String databaseName;

    public MongoTtlIndexFactory(MongoClient mongoClient,
                                @Value("${mongodb.database}") String databaseName) {
        this.mongoClient = mongoClient;
        this.databaseName = databaseName;
    }

    @EventListener
    public void onStartup(ServerStartupEvent event) {
        MongoDatabase db = mongoClient.getDatabase(databaseName);
        MongoCollection<?> history = db.getCollection("history");
        history.createIndex(
                Indexes.ascending("createdAt"),
                new IndexOptions().expireAfter(7L, TimeUnit.DAYS)
        );

        MongoCollection<?> historyProcessed = db.getCollection("history_processed");
        historyProcessed.createIndex(
                Indexes.ascending("createdAt"),
                new IndexOptions().expireAfter(7L, TimeUnit.DAYS)
        );
    }
}
