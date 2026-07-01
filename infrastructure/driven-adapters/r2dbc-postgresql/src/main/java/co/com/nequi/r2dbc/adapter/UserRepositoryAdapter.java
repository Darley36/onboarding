package co.com.nequi.r2dbc.adapter;

import co.com.nequi.model.enums.ErrorMessage;
import co.com.nequi.model.exception.ApplicationException;
import co.com.nequi.model.exception.BusinessException;
import co.com.nequi.model.user.User;
import co.com.nequi.model.user.gateways.UserDBGateway;
import co.com.nequi.r2dbc.UserRepository;
import co.com.nequi.r2dbc.entity.UserEntity;
import co.com.nequi.r2dbc.helper.ReactiveAdapterOperations;
import co.com.nequi.r2dbc.mapper.UserEntityMapper;
import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
public class UserRepositoryAdapter extends ReactiveAdapterOperations<
        User/* change for domain model */,
        UserEntity/* change for adapter model */,
        Integer,
        UserRepository
> implements UserDBGateway {

    private final R2dbcEntityTemplate entityTemplate;

    public UserRepositoryAdapter(UserRepository repository, ObjectMapper mapper, R2dbcEntityTemplate entityTemplate) {
        super(repository, mapper, d -> mapper.map(d, User.class/* change for domain model */));
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<User> saveUser(User user) {
        UserEntity userEntity = mapper.map(user, UserEntity.class);
        return entityTemplate.insert(userEntity)
                .map(savedEntity -> mapper.map(savedEntity, User.class))
                .doOnNext(userDB -> log.info("Usuario con ID: {} registrado exitosamente de la DB", userDB.getId()))
                .onErrorResume(throwable -> {
                    if (throwable instanceof DataIntegrityViolationException ||
                            throwable instanceof R2dbcDataIntegrityViolationException) {

                        return Mono.error(new BusinessException(
                                "El usuario proporcionado ya existe en el sistema. - Error:" + throwable.getMessage()
                                , ErrorMessage.ERROR_POSTGRES_DUPLICATE
                        ));
                    }
                    return Mono.error(new ApplicationException(
                            "Ocurrió un error inesperado al guardar el usuario en la base de datos. - Error:" + throwable.getMessage()
                            , ErrorMessage.ERROR_POSTGRES
                    ));
                });
    }

    @Override
    public Mono<User> getById(Integer id) {
        return repository.findById(id)
                .map(UserEntityMapper::toModel)
                .doOnNext(userDB -> log.info("Usuario con ID: {} recuperado exitosamente de la DB", userDB.getId()));
    }

    @Override
    public Flux<User> getAll(Integer size, Integer page) {
        int offset = size * page;

        return entityTemplate.select(UserEntity.class)
                .matching(Query.empty().limit(size).offset(offset))
                .all()
                .map(entity -> mapper.map(entity, User.class));
    }

    @Override
    public Flux<User> getByName(String name) {
        Criteria criterio = Criteria.where("first_name").is(name).ignoreCase(true);

        return entityTemplate.select(UserEntity.class)
                .matching(Query.query(criterio))
                .all()
                .map(entity -> mapper.map(entity, User.class))
                .doOnNext(userDB -> log.info("Usuario con nombre: {} recuperado exitosamente de la DB",
                        name));
    }
}
