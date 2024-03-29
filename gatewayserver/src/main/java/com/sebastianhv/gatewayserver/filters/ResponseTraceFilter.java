package com.sebastianhv.gatewayserver.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

@Configuration
public class ResponseTraceFilter {

    private static final Logger logger = LoggerFactory.getLogger(ResponseTraceFilter.class);

    @Autowired
    FilterUtility filterUtility;

//    We create a filter to pass the trace ID into the response header
    @Bean
    public GlobalFilter postGlobalFilter() {
        return (exchange, chain) -> {
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
//                We get the header from the request
                HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
//                We get the correlationId header
                String correlationId = filterUtility.getCorrelationId(requestHeaders);
                logger.debug("Updated the correlation id to the outbound headers. {}", correlationId);
//                We use the same correlationId into the response header
                exchange.getResponse().getHeaders().add(filterUtility.CORRELATION_ID, correlationId);
            }));
        };
    }
}
