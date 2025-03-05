package com.example.ficketsearch.domain.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutoCompleteRes {


    @JsonProperty("EventId")
    private String eventId;

    @JsonProperty("Title")
    private String title;  // 추천 검색어

}
