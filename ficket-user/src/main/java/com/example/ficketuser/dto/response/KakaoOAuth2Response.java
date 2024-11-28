package com.example.ficketuser.dto.response;

import java.util.Map;

public class KakaoOAuth2Response implements OAuth2Response {

    private final Map<String, Object> attribute;
    private final Map<String, Object> profile;

    public KakaoOAuth2Response(Map<String, Object> attribute){

        this.attribute = attribute;
        Map<String, Object> kakao_account = (Map<String, Object>) attribute.get("kakao_account");
        this.profile = (Map<String, Object>) kakao_account.get("profile");
    }

    @Override
    public Long getProviderId() {
        return (Long) attribute.get("id");
    }

    @Override
    public String getUserName() {
        return profile.get("nickname").toString();
    }
}
