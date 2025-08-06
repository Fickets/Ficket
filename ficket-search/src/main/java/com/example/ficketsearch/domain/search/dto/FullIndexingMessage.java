package com.example.ficketsearch.domain.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullIndexingMessage {
    private String s3UrlList;
    private boolean isFirstMessage;
    private boolean isLastMessage;
}