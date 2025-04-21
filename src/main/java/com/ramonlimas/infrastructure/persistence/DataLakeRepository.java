package com.ramonlimas.infrastructure.persistence;

import com.ramonlimas.domain.model.DataLake;
import com.ramonlimas.domain.model.HistoryEntity;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

@MongoRepository
public interface DataLakeRepository extends CrudRepository<DataLake, ObjectId> {
    Optional<DataLake> findByTitle(String name);
}
