package com.example.ficketadmin.domain.admin.mapper;


import com.example.ficketadmin.domain.admin.dto.common.AdminDto;
import com.example.ficketadmin.domain.admin.dto.common.AdminInfoDto;
import com.example.ficketadmin.domain.admin.dto.response.AdminLoginRes;
import com.example.ficketadmin.domain.admin.entity.Admin;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminMapper {

    AdminInfoDto toAdminInfoDto(Admin admin);

    @Mapping(target = "adminName", source = "name")
    AdminDto toAdminDto(Admin admin);
}
