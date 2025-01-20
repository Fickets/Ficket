package com.example.ficketsearch.domain.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ElasticsearchDTO {
    @JsonProperty("EventId")
    private String eventId;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Poster_Url")
    private String posterUrl;

    @JsonProperty("Stage")
    private String stage;

    @JsonProperty("Location")
    private String location;

    @JsonProperty("Genres")
    private List<Map<String, String>> genres;

    @JsonProperty("Schedules")
    private List<Map<String, String>> schedules;

    @JsonProperty("Ticketing")
    private LocalDateTime ticketing;
}