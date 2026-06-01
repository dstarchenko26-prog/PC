package ua.nulp.api_gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

	@Value("${AUTH_HOST:localhost}")
	private String authHost;

	@Value("${CATALOG_HOST:localhost}")
	private String catalogHost;

	@Value("${ORDER_HOST:localhost}")
	private String orderHost;

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				// Маршрути для Auth Service
				.route("auth-service-route", r -> r.path("/api/auth/**", "/api/users/**", "/api/sellers/**")
						.uri("http://" + authHost + ":8081"))

				// Маршрути для Catalog Service
				.route("catalog-service-route", r -> r.path("/api/products/**", "/api/reviews/**")
						.uri("http://" + catalogHost + ":8082"))

				// Маршрути для Order Service
				.route("order-service-route", r -> r.path("/api/orders/**", "/api/payments/**", "/api/commissions/**")
						.uri("http://" + orderHost + ":8083"))

				.build();
	}
}