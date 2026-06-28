package co.com.nequi.model.user.gateways;

import co.com.nequi.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserDBGateway {
    Mono<User> saveUser(User user);
    Mono<User> getById(Integer id);
    Flux<User> getAll(Integer size, Integer page);
    Flux<User> getByName(String name);
}
