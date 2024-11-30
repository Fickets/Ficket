package com.example.ficketevent.domain.event.dto.common;

import lombok.Data;

@Data
public class UserSimpleDto {

    private Long userId;

    private int birth;

    private Gender gender;

    private String userName;

    private Long socialId;
}
