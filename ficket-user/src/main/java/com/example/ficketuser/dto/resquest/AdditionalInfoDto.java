package com.example.ficketuser.dto.resquest;

import com.example.ficketuser.Entity.Gender;
import lombok.Data;


@Data
public class AdditionalInfoDto {

    int birth;

    Gender gender;
}
