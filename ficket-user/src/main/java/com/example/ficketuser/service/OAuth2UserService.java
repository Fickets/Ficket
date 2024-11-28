package com.example.ficketuser.service;


import com.example.ficketuser.Entity.User;
import com.example.ficketuser.repository.UserRepository;
import com.example.ficketuser.dto.response.KakaoOAuth2Response;
import com.example.ficketuser.dto.response.UserDto;
import com.example.ficketuser.dto.resquest.CustomOAuth2User;
import com.example.ficketuser.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        KakaoOAuth2Response oAuth2Response = new KakaoOAuth2Response(oAuth2User.getAttributes());
        Long kakaoProviderId = oAuth2Response.getProviderId();
        String kakaoUserName = oAuth2Response.getUserName();

        User user = userRepository.findBySocialId(kakaoProviderId)
                .orElseGet(() -> userService.saveUser(kakaoProviderId, kakaoUserName));


        UserDto userDto = userMapper.toUserDto(user);
        return new CustomOAuth2User(userDto);
    }
}
