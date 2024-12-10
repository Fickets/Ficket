package com.example.ficketticketing.global.config.feign;

import com.example.ficketticketing.global.result.error.FeignErrorDecoder;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder feignErrorDecoder() {
        return new FeignErrorDecoder();
    }

//    @Bean
//    public Encoder feignEncoder() {
//        return new SpringFormEncoder();
//    }

//    @Bean
//    public Decoder feignDecoder() {
//        return new JacksonDecoder(); // ObjectMapper 기반 디코더
//    }
}