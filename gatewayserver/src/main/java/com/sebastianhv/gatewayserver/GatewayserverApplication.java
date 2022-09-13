package com.sebastianhv.gatewayserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

import java.util.Date;

@SpringBootApplication
@EnableEurekaClient
public class GatewayserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayserverApplication.class, args);
	}

	@Bean
//	With the help of builder object, we can create any number of routes or custom requirements
	public RouteLocator myRoutes(RouteLocatorBuilder builder) {
//		We have 3 route configurations for Accounts, Loans and Cards
		return builder.routes()
//				This is a predicate function which will check whether the given path  satisfies the condition.
				.route(p -> p
//						If someone invokes the gateway with the following path
						.path("/eazybank/accounts/**")
//						Apply the filter, and rewrite the path into the following:
//						We take all the path after "...accounts/..." and we treat it as a segment
//						Then we just pass the segment
						.filters(f -> f.rewritePath("/eazybank/accounts/(?<segment>.*)","/${segment}")
//								We also add a custom header to the response with the name "X-Response-Time"
								.addResponseHeader("X-Response-Time", new Date().toString()))
//						Finally, redirect the request to the ACCOUNTS microservice
						.uri("lb://ACCOUNTS")).
				route(p -> p
						.path("/eazybank/loans/**")
						.filters(f -> f.rewritePath("/eazybank/loans/(?<segment>.*)","/${segment}")
								.addResponseHeader("X-Response-Time", new Date().toString()))
						.uri("lb://LOANS")).
				route(p -> p
						.path("/eazybank/cards/**")
						.filters(f -> f.rewritePath("/eazybank/cards/(?<segment>.*)","/${segment}")
								.addResponseHeader("X-Response-Time", new Date().toString()))
						.uri("lb://CARDS")).build();
	}

}
