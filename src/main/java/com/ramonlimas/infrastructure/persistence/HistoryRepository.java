package com.ramonlimas.infrastructure.persistence;

import com.ramonlimas.domain.model.HistoryEntity;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;
import org.bson.types.ObjectId;

import java.util.List;

@MongoRepository
public interface HistoryRepository extends CrudRepository<HistoryEntity, ObjectId> {

    List<HistoryEntity> findByUserId(String userId);
    List<HistoryEntity> findByUserId(String userId, Pageable pageable);
    Boolean existsByUserId(String userId);
    Long countByUserId(String userId);
    void deleteByUserId(String userId);
}
