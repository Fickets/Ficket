package com.example.ficketuser.domain.mapper;

import com.example.ficketuser.domain.Entity.User;
import com.example.ficketuser.domain.dto.UserSimpleDto;
import com.example.ficketuser.domain.dto.response.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserDto toUserDto(User user);

    UserSimpleDto toUserSimpleDto(User user);
}
