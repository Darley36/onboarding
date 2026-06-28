package co.com.nequi.api.router;

import co.com.nequi.api.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(UserHandler handler) {
        return RouterFunctions.route()
                .path("/api/v1", builder -> builder
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON)
                                .or(RequestPredicates.contentType(MediaType.APPLICATION_JSON)),
                                nestedBuilder -> nestedBuilder
                                        .POST("/user/create/{id}", handler::saveUser)
                                        .GET("/user/get", handler::getById)
                                        .GET("/user/getall", handler::getAllUser)
                                        .GET("/user/getall/name", handler::getByName)
                        )
                ).build();
    }
}
