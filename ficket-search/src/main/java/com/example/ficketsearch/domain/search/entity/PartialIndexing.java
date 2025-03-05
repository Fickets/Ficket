package com.example.ficketsearch.domain.search.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "partial-indexing")
public class PartialIndexing {
    @Id
    private String id;

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

    private String operationType;

    private boolean indexed;
}
