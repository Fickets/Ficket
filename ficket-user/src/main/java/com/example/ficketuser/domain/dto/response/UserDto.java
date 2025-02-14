package com.example.ficketuser.domain.dto.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class UserDto implements Serializable {
    private Long userId;
    private String userName;
    private Long socialId;
}
