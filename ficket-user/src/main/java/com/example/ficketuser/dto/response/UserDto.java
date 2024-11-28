package com.example.ficketuser.dto.response;


import com.example.ficketuser.Entity.Gender;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserDto {

    private Long userId;
    private String userName;
    private Long socialId;
}
