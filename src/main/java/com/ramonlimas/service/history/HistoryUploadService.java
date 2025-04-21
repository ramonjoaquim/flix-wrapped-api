package com.ramonlimas.service.history;

import com.ramonlimas.domain.model.HistoryUploadEntity;
import com.ramonlimas.infrastructure.persistence.HistoryUploadRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bson.types.ObjectId;

import java.util.Optional;

@Singleton
public class HistoryUploadService {

    @Inject
    private HistoryUploadRepository repository;

    public Optional<HistoryUploadEntity> findById(ObjectId id) {
        return repository.findById(id);
    }
}
