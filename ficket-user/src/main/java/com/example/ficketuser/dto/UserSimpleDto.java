package com.example.ficketuser.dto;


import com.example.ficketuser.Entity.Gender;
import com.querydsl.core.annotations.QueryProjection;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import lombok.Data;

@Data
public class UserSimpleDto {

    private Long userId;

    private int birth;

    private Gender gender;

    private String userName;

    private Long socialId;


    @QueryProjection
    public UserSimpleDto(Long userId, int birth, Gender gender, String userName, Long socialId) {
        this.userId = userId;
        this.birth = birth;
        this.gender = gender;
        this.userName = userName;
        this.socialId = socialId;
    }
}
