package com.ramonlimas.infrastructure.persistence;

import com.ramonlimas.domain.enums.UploadStatus;
import com.ramonlimas.domain.model.HistoryUploadEntity;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;
import org.bson.types.ObjectId;

import java.util.Optional;


@MongoRepository
public interface HistoryUploadRepository extends CrudRepository<HistoryUploadEntity, ObjectId> {
    Optional<HistoryUploadEntity> findByUserIdAndStatus(String userId, UploadStatus status);
}
