package com.example.ficketuser.domain.dto.resquest;

import com.example.ficketuser.domain.Entity.Gender;
import lombok.Data;


@Data
public class AdditionalInfoDto {

    int birth;

    Gender gender;
}
