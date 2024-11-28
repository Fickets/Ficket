package com.example.ficketuser.dto.resquest;

import com.example.ficketuser.Entity.Gender;
import com.example.ficketuser.dto.response.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final UserDto userDto;


    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getName() {
        return userDto.getUserName();
    }

    public Long getSocialId() {
        return userDto.getSocialId();
    }

    public Long getUserId() {
        return userDto.getUserId();
    }
}
