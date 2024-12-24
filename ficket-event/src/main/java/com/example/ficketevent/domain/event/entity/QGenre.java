package com.example.ficketevent.domain.event.entity;

import com.example.ficketevent.domain.event.enums.Genre;
import com.querydsl.core.types.dsl.EnumPath;

public class QGenre extends EnumPath<Genre> {
    public static final QGenre genre = new QGenre("genre");

    public QGenre(String variable) {
        super(Genre.class, variable);
    }
}
