package com.example.ficketuser.dto.resquest;

import com.example.ficketuser.Entity.Gender;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private Long userId;
    private String userName;
    private int birth;
    private Gender gender;
}
