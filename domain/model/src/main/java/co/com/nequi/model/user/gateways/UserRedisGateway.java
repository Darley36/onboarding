package co.com.nequi.model.user.gateways;

import co.com.nequi.model.user.User;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserRedisGateway {
    Mono<User> getUserFromCache(Integer id);
    Mono<User> saveUserToCache(Integer id, User user);
    Mono<List<User>> getUsersByNameFromCache(String name);
    Mono<List<User>> saveUsersByNameToCache(String name, List<User> users);
}
