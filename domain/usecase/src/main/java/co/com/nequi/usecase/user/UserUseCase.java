package co.com.nequi.usecase.user;

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
                .flatMap(user -> Mono.error(new RuntimeException("User already exist")))
                .switchIfEmpty(Mono.defer(() -> userWebClientGateway.getById(id)
                        .flatMap(userDBGateway::save)))
                .cast(User.class);
    }

    public Mono<User> getById(Integer id){
        return userDBGateway.getById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("the user not found")));
    }

    public Flux<User> getAll(){
        return userDBGateway.getAll();
    }

}
