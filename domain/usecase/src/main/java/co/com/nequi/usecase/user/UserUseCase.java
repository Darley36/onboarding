package co.com.nequi.usecase.user;

import co.com.nequi.model.enums.ErrorMessage;
import co.com.nequi.model.exception.BusinessException;
import co.com.nequi.model.user.User;
import co.com.nequi.model.user.gateways.UserDBGateway;
import co.com.nequi.model.user.gateways.UserWebClientGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserUseCase {

    private final UserDBGateway userDBGateway;
    private final UserWebClientGateway userWebClientGateway;

    public Mono<User> saveUserById(Integer id){
        return userDBGateway.getById(id)
                .flatMap(user -> Mono.error(new BusinessException(
                                "El usuario proporcionado ya existe en el sistema."
                                , ErrorMessage.ERROR_POSTGRES_DUPLICATE
                        )))
                .switchIfEmpty(Mono.defer(() -> userWebClientGateway.getById(id)
                        .flatMap(userDBGateway::saveUser)))
                .cast(User.class);
    }

    public Mono<User> getById(Integer id){
        return userDBGateway.getById(id)
                .switchIfEmpty(Mono.error(new BusinessException("El usuario no fue encontrado", ErrorMessage.ERROR_POSTGRES_NOT_FOUND)));
    }

    public Flux<User> getAll(Integer size, Integer page){
        return userDBGateway.getAll(size, page);
    }

    public Flux<User> getByName(String name){
        return userDBGateway.getByName(name)
                .switchIfEmpty(Mono.error(new BusinessException("No existe ningun usuario con ese nombre",
                        ErrorMessage.ERROR_POSTGRES_NOT_FOUND)));
    }

}
