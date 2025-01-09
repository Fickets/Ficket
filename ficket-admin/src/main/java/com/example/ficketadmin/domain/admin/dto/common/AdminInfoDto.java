package com.example.ficketadmin.domain.admin.dto.common;

import com.example.ficketadmin.domain.admin.entity.Role;
import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class AdminInfoDto {
    long adminId;
    String id;
    String pw;
    String name;
    Role role;
}
