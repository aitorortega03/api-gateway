package com.example.api_gateway.services;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

@Service
public class RouterValidator {

  public static final List<String> openEndpoints = List.of(
    "/v1/auth/**",
    "/api/v1/games"
  );

  public Predicate<ServerHttpRequest> isSecured = request ->
    openEndpoints.stream().noneMatch(uri -> request.getURI().getPath().contains(uri));

}
