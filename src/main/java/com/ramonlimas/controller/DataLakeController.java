package com.ramonlimas.controller;


import com.ramonlimas.domain.dto.FlixWrappedResponse;
import com.ramonlimas.domain.dto.MetadadoDTO;
import com.ramonlimas.domain.model.DataLake;
import com.ramonlimas.infrastructure.persistence.DataLakeRepository;
import io.micronaut.cache.annotation.Cacheable;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

import java.util.Optional;

@Controller("/flix-wrapped/api/datalake")
@Tag(name = "DataLake", description = "Endpoints para acessar os metadados de filmes e séries")
public class DataLakeController {

    @Inject
    private DataLakeRepository dataLakeRepository;

    @Cacheable("datalake-cache")
    @Get("metadado/{title}")
    @Operation(
            summary = "Obter o metadado do filme ou serie pelo titulo",
            description = "Retorna os metadados salvos no datalake.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Metadado retornado com sucesso",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = FlixWrappedResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Metadado não encontrado",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public FlixWrappedResponse<?> getMetadadobyTitle(@PathVariable String title) {
        Optional<DataLake> byTitle = dataLakeRepository.findByTitle(title);

        if (byTitle.isPresent()) {
            DataLake meta = byTitle.get();
            MetadadoDTO dto = new MetadadoDTO(meta.getTitle(), meta.getUrlPoster(), meta.getReleaseYear());
            return FlixWrappedResponse.ok(dto).body();
        }

        return FlixWrappedResponse.notFound().body();
    }
}
