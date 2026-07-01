package co.com.nequi.redis.adapter;

import co.com.nequi.model.user.User;
import co.com.nequi.model.user.gateways.UserRedisGateway;
import co.com.nequi.redis.template.ReactiveRedisTemplateAdapter;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class UserRedisAdapter implements UserRedisGateway {

    private final ReactiveRedisTemplateAdapter redisTemplate;
    private final ObjectMapper objectMapper;

    public UserRedisAdapter(ReactiveRedisTemplateAdapter redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private static final String CACHE_PREFIX = "user:";
    private static final long EXPIRATION_MS = 300000;

    @Override
    public Mono<User> saveUserToCache(Integer id, User user) {
        String key = CACHE_PREFIX+id;
        return redisTemplate.save(key, user, EXPIRATION_MS)
                .cast(User.class)
                .doOnNext(userCache -> log.info("Usuario con ID: {} guardado exitosamente en la CACHÉ", id))
                .onErrorResume(throwable -> {
                    log.warn("Error al guardar en redis, error :"+throwable.getMessage());
                    return Mono.just(user);
                });
    }

    @Override
    public Mono<User> getUserFromCache(Integer id) {
        String key = CACHE_PREFIX + id;
        return redisTemplate.findById(key)
                .flatMap(rawObject -> {
                    User user = objectMapper.map(rawObject, User.class);
                    return Mono.just(user);
                })
                .doOnNext(user -> log.info("Usuario con ID: {} recuperado exitosamente de la CACHÉ", id))
                .onErrorResume(throwable -> {
                    log.warn("Error al consultar en redis, error :"+throwable.getMessage());
                    return Mono.empty();
                });
    }

    @Override
    public Mono<List<User>> saveUsersByNameToCache(String name, List<User> users) {
        String key = CACHE_PREFIX+name.toLowerCase();

        return redisTemplate.save(key, users, EXPIRATION_MS)
                .map(object -> users)
                .doOnNext(userCache -> log.info("Usuarios con nombre: {} guardado exitosamente en la CACHÉ", name))
                .thenReturn(users)
                .onErrorResume(throwable -> {
                    log.warn("Error al guardar en redis, error :"+throwable.getMessage());
                    return Mono.just(users);
                });
    }

    @Override
    public Mono<List<User>> getUsersByNameFromCache(String name) {
        String key = CACHE_PREFIX + name.toLowerCase();

        return redisTemplate.findById(key)
                .map(cachedObject -> {
                    List<?> rawList = objectMapper.map(cachedObject, List.class);

                    return rawList.stream()
                            .map(element -> objectMapper.map(element, User.class))
                            .toList();
                })
                .doOnNext(user -> log.info("Usuarios con nombre: {} recuperado exitosamente de la CACHÉ", name))
                .onErrorResume(throwable -> {
                    log.warn("Error al consultar en redis, error :"+throwable.getMessage());
                    return Mono.empty();
                });
    }
}
