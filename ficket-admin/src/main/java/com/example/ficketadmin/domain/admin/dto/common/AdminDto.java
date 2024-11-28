package com.example.ficketadmin.domain.admin.dto.common;

import lombok.Data;

@Data
public class AdminDto {
    private Long adminId;
    private String adminName;
    private String adminRole;
}