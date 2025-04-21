package com.ramonlimas.service.insights;

import com.ramonlimas.domain.dto.InsightResultDTO;
import com.ramonlimas.domain.enums.InsightType;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class InsightsService {

    private final Map<InsightType, Insight> insightMap;

    public InsightsService(List<Insight> insights) {
        this.insightMap = insights.stream()
                .collect(Collectors.toMap(Insight::getType, Function.identity()));
    }

    public List<InsightResultDTO> getAllInsights(String userId, boolean includeTitles) {
        return insightMap.values().stream()
                .map(i -> i.process(userId, includeTitles))
                .collect(Collectors.toList());
    }

    public Optional<InsightResultDTO> getInsightByType(String userId, InsightType type, boolean includeTitles) {
        Insight processor = insightMap.get(type);
        return Optional.ofNullable(processor).map(p -> p.process(userId, includeTitles));
    }
}
