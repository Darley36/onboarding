package co.com.nequi.api.handler;

import co.com.nequi.api.exception.ErrorHandler;
import co.com.nequi.model.enums.ErrorMessage;
import co.com.nequi.model.exception.ValidationException;
import co.com.nequi.usecase.user.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;


@Component
@RequiredArgsConstructor
public class UserHandler {

    private final UserUseCase userUseCase;
    private final ErrorHandler errorHandler;

    public Mono<ServerResponse> saveUser(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return validateParam(id, "id")
                .flatMap(userUseCase::saveUserById)
                .flatMap(userCreated -> ServerResponse.ok()
                        .bodyValue(userCreated))
                .onErrorResume(throwable -> errorHandler.handleError(throwable, serverRequest));
    }

    private Mono<Integer> validateParam(String param, String paramName){
        return Mono.just(param)
                .filter(s -> !s.isBlank() && s.matches("\\d+"))
                .map(Integer::parseInt)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new ValidationException(
                        "el parámetro "+paramName+" es obligatorio y debe ser numerico",
                        ErrorMessage.ERROR_FIELDS
                ))));
    }

    public Mono<ServerResponse> getById(ServerRequest serverRequest) {
        String id = serverRequest.queryParam("id").orElse("");
        return validateParam(id, "id")
                .flatMap(userUseCase::getById)
                .flatMap(userSearch -> ServerResponse.ok()
                        .bodyValue(userSearch))
                .onErrorResume(throwable -> errorHandler.handleError(throwable, serverRequest));
    }

    public Mono<ServerResponse> getAllUser(ServerRequest serverRequest) {
        return Mono.just(serverRequest)
                .flatMap(request -> {
                    Mono<Integer> sizeMono = validateParam(request.queryParam("tamano").orElse(""), "tamano");
                    Mono<Integer> pageMono = validateParam(request.queryParam("pagina").orElse(""), "pagina");
                    return Mono.zip(sizeMono, pageMono);
                })
                .flatMap(tuple -> userUseCase.getAll(tuple.getT1(), tuple.getT2())
                        .collectList())
                .flatMap(users -> ServerResponse.ok().bodyValue(users))
                .onErrorResume(throwable -> errorHandler.handleError(throwable, serverRequest));
    }

    public Mono<ServerResponse> getByName(ServerRequest serverRequest) {
        String name = serverRequest.queryParam("name").orElse("");
        return Mono.just(name)
                .filter(s -> !s.isBlank())
                .switchIfEmpty(Mono.defer(() -> Mono.error(new ValidationException(
                        "el parámetro nombre es obligatorio ",
                        ErrorMessage.ERROR_FIELDS
                ))))
                .flatMap(nam -> userUseCase.getByName(nam).collectList())
                .flatMap(userSearch -> ServerResponse.ok()
                        .bodyValue(userSearch))
                .onErrorResume(throwable -> errorHandler.handleError(throwable, serverRequest));
    }

}
