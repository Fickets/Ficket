package com.example.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 이 클래스는 요청과 응답에 대해 사용자 정의 필터를 적용하는 클래스입니다.
 * 요청 전후에 로깅을 수행하여 요청 ID와 응답 상태 코드를 출력합니다.
 */
@Component
@Slf4j
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {

	public CustomFilter() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		// 사용자 정의 Pre 필터 - 요청 처리 전에 실행됩니다.
		return (exchange, chain) -> {
			// 요청 및 응답 객체를 가져옵니다.
			ServerHttpRequest request = exchange.getRequest();
			ServerHttpResponse response = exchange.getResponse();

			// 요청 ID를 로깅 (PRE 필터)
			log.info("사용자 정의 PRE 필터: 요청 ID -> {}", request.getId());

			// 사용자 정의 Post 필터 - 응답 처리 후에 실행됩니다.
			return chain.filter(exchange).then(Mono.fromRunnable(() -> {
				// 응답 상태 코드를 로깅 (POST 필터)
				log.info("사용자 정의 POST 필터: 응답 상태 코드 -> {}", response.getStatusCode());
			}));
		};
	}

	/**
	 * 필터 설정 정보 클래스 (Config) - 필요시 필터의 설정 속성을 정의할 수 있습니다.
	 */
	public static class Config {
		// 필터에 필요한 설정 속성 정의
	}
}
