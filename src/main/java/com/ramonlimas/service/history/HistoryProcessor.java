package com.ramonlimas.service.history;

import com.ramonlimas.domain.dto.HistoryCSV;
import com.ramonlimas.domain.enums.UploadStatus;
import com.ramonlimas.domain.model.HistoryEntity;
import com.ramonlimas.domain.model.HistoryUploadEntity;
import com.ramonlimas.infrastructure.persistence.HistoryRepository;
import com.ramonlimas.infrastructure.persistence.HistoryUploadRepository;
import com.ramonlimas.processor.RawHistoryProcessor;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.exceptions.NotFoundException;
import io.micronaut.scheduling.annotation.Async;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.bson.types.ObjectId;

import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class HistoryProcessor {

    @Inject
    HistoryRepository historyRepository;

    @Inject
    HistoryUploadRepository historyUploadRepository;

    @Inject
    HistoryService service;

    @Inject
    private RawHistoryProcessor rawHistoryProcessor;

    @Async
    public void processCsvAsync(CompletedFileUpload file, String userId, HistoryUploadEntity historyUploadEntity) throws Exception {
        try {
            List<HistoryCSV> historyCSVS = processCsv(file, userId);
            if (service.existsByUserId(userId)) {
                syncUserHistory(historyCSVS);
                setHistoryUploadFinished(historyUploadEntity, UploadStatus.COMPLETED, null);
                rawHistoryProcessor.process(userId);
                return;
            }
            service.saveAll(historyCSVS);
            setHistoryUploadFinished(historyUploadEntity, UploadStatus.COMPLETED, null);
            rawHistoryProcessor.process(userId);
        } catch (Exception e) {
            log.error("Erro ao processar CSV", e);
            setHistoryUploadFinished(historyUploadEntity, UploadStatus.ERROR, e.getMessage());
        }
    }

    public HistoryUploadEntity getStatusUpload(String uploadId) {
        Optional<HistoryUploadEntity> historyUpload = historyUploadRepository.findById(new ObjectId(uploadId));
        return historyUpload.orElseThrow(NotFoundException::new);
    }

    public HistoryUploadEntity createHistoryUpload(String userId) {
        Optional<HistoryUploadEntity> optionalHistoryUpload = historyUploadRepository.findByUserIdAndStatus(userId, UploadStatus.IN_PROGRESS);
        optionalHistoryUpload.ifPresent(ohu -> {
            throw new RuntimeException("Existe um upload com id: " + ohu.getId() + " em progresso, aguarde a conclusão");
        });

        HistoryUploadEntity historyUploadEntity = new HistoryUploadEntity();
        historyUploadEntity.setUserId(userId);
        historyUploadEntity.setStatus(UploadStatus.IN_PROGRESS);
        historyUploadEntity.setCreatedAt(Date.from(Instant.now()));
        return historyUploadRepository.save(historyUploadEntity);
    }

    private void setHistoryUploadFinished(HistoryUploadEntity historyUploadEntity, UploadStatus status, String messageError) {
        historyUploadEntity.setConcludeAt(Date.from(Instant.now()));
        historyUploadEntity.setStatus(status);
        historyUploadEntity.setError(messageError);
        historyUploadRepository.update(historyUploadEntity);
    }

    public List<HistoryCSV> processCsv(CompletedFileUpload file, String userId) throws Exception {
        CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(new InputStreamReader(file.getInputStream()));

        return parser.getRecords().stream()
                .map(record -> HistoryCSV.fromCsv(record, userId))
                .toList();
    }

    public void syncUserHistory(List<HistoryCSV> entries) {
        if (entries.isEmpty()) return;

        String userId = entries.get(0).userId();  // Ajuste para acessar o primeiro registro de forma mais eficiente

        // Buscar todos os registros atuais do usuário
        List<HistoryEntity> existingRecords = historyRepository.findByUserId(userId);

        // Converter os novos registros para uma lista de entidades
        Set<HistoryEntity> newEntities = entries.stream()
                .map(entry -> new HistoryEntity(entry.userId(), entry.title(), entry.date()))
                .collect(Collectors.toSet());  // Usando Set para evitar duplicação e melhorar a comparação

        // Usar Sets para comparar de forma mais eficiente
        Set<String> existingTitlesAndDates = existingRecords.stream()
                .map(existing -> existing.getTitle() + ":" + existing.getDate())  // Combinando título e data em uma string
                .collect(Collectors.toSet());

        // Operações de adição em lote
        List<HistoryEntity> toSave = newEntities.stream()
                .filter(newEntity -> !existingTitlesAndDates.contains(newEntity.getTitle() + ":" + newEntity.getDate()))
                .collect(Collectors.toList());

        if (!toSave.isEmpty()) {
            historyRepository.saveAll(toSave);  // Salvando todos de uma vez
        }

        // Operações de remoção em lote
        Set<String> newTitlesAndDates = newEntities.stream()
                .map(newEntity -> newEntity.getTitle() + ":" + newEntity.getDate())
                .collect(Collectors.toSet());

        List<HistoryEntity> toDelete = existingRecords.stream()
                .filter(existing -> !newTitlesAndDates.contains(existing.getTitle() + ":" + existing.getDate()))
                .collect(Collectors.toList());

        if (!toDelete.isEmpty()) {
            historyRepository.deleteAll(toDelete);  // Removendo todos de uma vez
        }
    }
}
