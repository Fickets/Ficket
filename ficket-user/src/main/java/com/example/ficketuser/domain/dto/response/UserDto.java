package com.example.ficketuser.domain.dto.response;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserDto {

    private Long userId;
    private String userName;
    private Long socialId;
}
