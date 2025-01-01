package com.example.ficketevent.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class SimplePageRes<T> {
    private List<SimpleEvent> content;
    private int page;
    private int size;
    private boolean last;
}
