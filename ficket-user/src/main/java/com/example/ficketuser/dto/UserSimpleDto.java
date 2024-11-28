package com.example.ficketuser.dto;


import com.example.ficketuser.Entity.Gender;
import lombok.Data;

@Data
public class UserSimpleDto {

    private Long userId;

    private int birth;

    private Gender gender;

    private String userName;

    private Long socialId;
}
