package com.example.api_gateway.services;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

  private final RouterValidator validator;

  private final JwtUtils jwtUtils;

  public AuthenticationFilter(RouterValidator validator, JwtUtils jwtUtils) {
    super(Config.class);
    this.validator = validator;
    this.jwtUtils = jwtUtils;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return ((exchange, chain) -> {
      var request = exchange.getRequest();

      ServerHttpRequest serverHttpRequest = null;

      if (validator.isSecured.test(request)){
        if (isAuthMissing(request)) {
          return onError(exchange);
        }
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (Objects.nonNull(authHeader) && authHeader.startsWith("Bearer ")) {
          authHeader = authHeader.substring(7);
        } else {
          return onError(exchange);
        }

        if (jwtUtils.isExpired(authHeader)) {
          return onError(exchange);
        }

        serverHttpRequest = exchange.getRequest()
          .mutate()
          .header("userIdRequest", jwtUtils.getUserIdFromToken(authHeader).toString())
          .build();
      }
      return chain.filter(exchange.mutate().request(serverHttpRequest).build());
    });
  }

  private Mono<Void> onError(ServerWebExchange exchange) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    return null;
  }

  private boolean isAuthMissing(ServerHttpRequest request) {
    return !request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
  }

  public static class Config {}
}
