package com.sebastianhv.gatewayserver.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

//We can choose the order of execution of our filters
@Order(1)
//We add this annotation so that Spring Boot will detect it and auto wire it
@Component
//We have to implement de GlobalFilter interface
public class TraceFilter implements GlobalFilter {

//    We define a logger
    private static final Logger logger = LoggerFactory.getLogger(TraceFilter.class);

//    Utility class we created
    @Autowired
    FilterUtility filterUtility;

//    Checks if there is a correlationId, if there is not, it creates it.
//    Accepts request of ServerWebExchange type and GatewayFilterChain
    @Override
//    After processing the request inside this business logic, we hand over the "exchange" to the next filter
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        Get the headers from the given request
        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
//        If there is a Correlation Id, we notify it
        if (isCorrelationIdPresent(requestHeaders)) {
            logger.debug("EazyBank-correlation-id found in tracing filter: {}. ",
                    filterUtility.getCorrelationId(requestHeaders));
//            If we don't, we create it. That is, because we receive the request from the user for the first time
        } else {
            String correlationID = generateCorrelationId();
            exchange = filterUtility.setCorrelationId(exchange, correlationID);
            logger.debug("EazyBank-correlation-id generated in tracing filter: {}. ", correlationID);
        }
        return chain.filter(exchange);
    }

    private boolean isCorrelationIdPresent(HttpHeaders requestHeaders) {
        if (filterUtility.getCorrelationId(requestHeaders) != null) {
            return  true;
        } else {
            return false;
        }
    }

//    Generate a random alphanumeric value as trace ID
    private String generateCorrelationId() {
        return java.util.UUID.randomUUID().toString();
    }
}
