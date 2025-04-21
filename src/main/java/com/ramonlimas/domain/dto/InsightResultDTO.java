package com.ramonlimas.domain.dto;

import com.ramonlimas.domain.enums.InsightType;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Serdeable
public class InsightResultDTO {
    private InsightType insightType;
    private String message;
    private Map<String, Object> data;

    public InsightResultDTO(InsightType insightType, String message, Map<String, Object> data) {
        this.insightType = insightType;
        this.message = message;
        this.data = data;
    }

    @Getter
    @Setter
    @Serdeable
    public static class SeriesInfo {
        private String title;
        private int episodesCount;
        private String urlPoster;

        public SeriesInfo(String title, int episodesCount, String urlPoster) {
            this.title = title;
            this.episodesCount = episodesCount;
            this.urlPoster = urlPoster;
        }
    }

    @Getter
    @Setter
    @Serdeable
    public static class YearlyContentConsumptionDTO {
        private int year;
        private int moviesCount;
        private int episodesCount;
        private List<String> moviesTitles;
        private List<String> seriesTitles;

        public YearlyContentConsumptionDTO(int year, int moviesCount, int episodesCount) {
            this.year = year;
            this.moviesCount = moviesCount;
            this.episodesCount = episodesCount;
        }
    }
}
