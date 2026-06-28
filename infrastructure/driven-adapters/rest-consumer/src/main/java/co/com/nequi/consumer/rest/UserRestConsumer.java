package co.com.nequi.consumer.rest;

import co.com.nequi.consumer.mapper.UserResponseMapper;
import co.com.nequi.consumer.response.UserResponse;
import co.com.nequi.model.enums.ErrorMessage;
import co.com.nequi.model.exception.ApplicationException;
import co.com.nequi.model.exception.BusinessException;
import co.com.nequi.model.user.User;
import co.com.nequi.model.user.gateways.UserWebClientGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRestConsumer implements UserWebClientGateway {

    private final WebClient webClient;

    @Override
    public Mono<User> getById(Integer id) {
        return webClient
                .get()
                .uri("/"+id)
                .header("x-api-key", "free_user_3FayyrNhVKGMdHO3q9MydkqkGxC")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse ->{
                            if (clientResponse.statusCode().value() == 404) return Mono.error(new BusinessException(
                                    "External user not exists:" + clientResponse,
                                    ErrorMessage.ERROR_NOT_FOUND_EXTERNAL_SERVICE));
                            return Mono.error(new BusinessException("Por favor, verifica la información e inténtalo de " +
                                    "nuevo - Servicio externo:" + clientResponse.statusCode().toString(),
                                    ErrorMessage.ERROR_UNAUTHORIZED_EXTERNAL_SERVICE));
                        })
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(bodyResponse -> Mono.error(new ApplicationException("Error en el sistema " +
                                        "externo -" + bodyResponse, ErrorMessage.ERROR_INTERNAL))))
                .bodyToMono(UserResponse.class)
                .map(response -> {
                    if (response.getData() == null) {
                        log.warn("Respuesta inválida del sistema externo: {}", response);
                        throw new ApplicationException("Respuesta inválida del sistema externo", ErrorMessage.ERROR_INTERNAL);
                    }
                    return UserResponseMapper.toUser(response);
                });
    }
}
