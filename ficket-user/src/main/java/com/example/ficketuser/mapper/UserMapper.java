package com.example.ficketuser.mapper;

import com.example.ficketuser.Entity.User;
import com.example.ficketuser.dto.UserSimpleDto;
import com.example.ficketuser.dto.response.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserDto toUserDto(User user);

    UserSimpleDto toUserSimpleDto(User user);
}
