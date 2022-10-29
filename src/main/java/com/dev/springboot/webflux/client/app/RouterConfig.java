package com.dev.springboot.webflux.client.app;

import com.dev.springboot.webflux.client.app.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductHandler handler) {
        return RouterFunctions
            .route(RequestPredicates.GET("/api/client"),
                request -> handler.list(request)
            )
            .andRoute(RequestPredicates.GET("/api/client/{id}"),
                request -> handler.detail(request)
            )
            .andRoute(RequestPredicates.POST("/api/client"),
                request -> handler.create(request)
            )
            .andRoute(RequestPredicates.PUT("/api/client/{id}"),
                request -> handler.edit(request)
            )
            .andRoute(RequestPredicates.DELETE("/api/client/{id}"),
                request -> handler.delete(request)
            )
            .andRoute(RequestPredicates.POST("/api/client/upload/{id}"),
                request -> handler.upload(request)
            );
    }
}
