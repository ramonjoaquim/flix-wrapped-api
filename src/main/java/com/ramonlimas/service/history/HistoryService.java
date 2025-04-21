package com.ramonlimas.service.history;

import com.ramonlimas.domain.dto.HistoryCSV;
import com.ramonlimas.domain.model.HistoryEntity;
import com.ramonlimas.infrastructure.persistence.HistoryRepository;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class HistoryService {

    private final HistoryRepository repository;

    public HistoryService(HistoryRepository repository) {
        this.repository = repository;
    }

    public void saveAll(List<HistoryCSV> entries) {
        List<HistoryEntity> entities = entries.stream()
                .map(entry -> new HistoryEntity(entry.userId(), entry.title(), entry.date()))
                .toList();

        repository.saveAll(entities);
    }

    public void save(HistoryEntity entity) {
        repository.save(entity);
    }

    public List<HistoryEntity> findByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    public Boolean existsByUserId(String email) {
        return repository.existsByUserId(email);
    }

}
