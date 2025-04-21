package com.ramonlimas.controller;

import com.ramonlimas.domain.dto.InsightResultDTO;
import com.ramonlimas.domain.enums.InsightType;
import com.ramonlimas.service.insights.InsightsService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller("/flix-wrapped/api/insights")
@Tag(name = "Insights")
public class InsightsController {

    private final InsightsService insightsService;

    @Inject
    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @ExecuteOn(TaskExecutors.BLOCKING)
    @Get("/{userId}")
    @Operation(summary = "Retorna insights personalizados",
            description = "Retorna todos os insights ou um insight específico baseado no tipo informado.")
    public List<InsightResultDTO> getAllInsights(
            @Parameter(description = "UserId do usuário") @PathVariable String userId,
            @Parameter(description = "Tipo de insight a ser retornado") @QueryValue Optional<InsightType> type,
            @Parameter(description = "Retornar lista de filmes e séries quandl aplicável") @QueryValue(defaultValue = "false") boolean includeTitles) {

        return type.map(t -> insightsService.getInsightByType(userId, t, includeTitles)
                        .map(List::of)
                        .orElse(Collections.emptyList()))
                .orElseGet(() -> insightsService.getAllInsights(userId, includeTitles));
    }
}