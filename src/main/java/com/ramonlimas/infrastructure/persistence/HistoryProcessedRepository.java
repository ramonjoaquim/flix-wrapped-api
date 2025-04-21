package com.ramonlimas.infrastructure.persistence;

import com.ramonlimas.domain.model.HistoryProcessedEntity;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;
import org.bson.types.ObjectId;

import java.util.List;

@MongoRepository
public interface HistoryProcessedRepository extends CrudRepository<HistoryProcessedEntity, ObjectId> {
    List<HistoryProcessedEntity> findByUserId(String userId);
    void deleteByUserId(String userId);
}
