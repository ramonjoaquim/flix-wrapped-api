package com.ramonlimas.controller;

import com.ramonlimas.domain.dto.FlixWrappedResponse;
import com.ramonlimas.domain.dto.HistoryUploadResponseDTO;
import com.ramonlimas.domain.model.HistoryUploadEntity;
import com.ramonlimas.service.history.HistoryProcessor;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

@Controller("/flix-wrapped/api/upload")
@Tag(name = "Upload", description = "Endpoints para upload de arquivos e verificação de status")
public class UploadController {

    @Inject
    HistoryProcessor historyProcessor;

    @Post(consumes = {MediaType.MULTIPART_FORM_DATA, MediaType.TEXT_CSV})
    @Operation(
            summary = "Enviar arquivo CSV para processamento",
            description = "Este endpoint aceita um arquivo CSV e um userId para iniciar o processamento assíncrono do histórico.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Upload iniciado com sucesso",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Erro na validação dos dados fornecidos",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public HttpResponse<FlixWrappedResponse<Object>> uploadFile(@Part("file") CompletedFileUpload file,
                                                                @Part("userId") String userId) throws Exception {

        HistoryUploadEntity historyUploadEntity = historyProcessor.createHistoryUpload(userId);
        historyProcessor.processCsvAsync(file, userId, historyUploadEntity);

        return FlixWrappedResponse.ok("Upload iniciado, use o ID " + historyUploadEntity.getId() + " para verificar o status.");
    }

    @Get("status/{uploadId}")
    @Operation(
            summary = "Consultar status do upload",
            description = "Retorna o status do processamento do histórico de upload com base no ID fornecido.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Status do upload retornado com sucesso",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = HistoryUploadResponseDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "ID de upload não encontrado",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    public HttpResponse<FlixWrappedResponse<HistoryUploadResponseDTO>> checkUploadStatus(@PathVariable String uploadId) {
        HistoryUploadEntity historyUploadEntity = historyProcessor.getStatusUpload(uploadId);

        return FlixWrappedResponse.ok(new HistoryUploadResponseDTO(historyUploadEntity));
    }
}
