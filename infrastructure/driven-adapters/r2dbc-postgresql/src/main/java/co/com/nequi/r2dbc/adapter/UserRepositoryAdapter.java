package co.com.nequi.r2dbc.adapter;

import co.com.nequi.model.user.User;
import co.com.nequi.model.user.gateways.UserDBGateway;
import co.com.nequi.r2dbc.UserRepository;
import co.com.nequi.r2dbc.entity.UserEntity;
import co.com.nequi.r2dbc.helper.ReactiveAdapterOperations;
import co.com.nequi.r2dbc.mapper.UserEntityMapper;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class UserRepositoryAdapter extends ReactiveAdapterOperations<
        User/* change for domain model */,
        UserEntity/* change for adapter model */,
        Integer,
        UserRepository
> implements UserDBGateway {
    public UserRepositoryAdapter(UserRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, User.class/* change for domain model */));
    }

    @Override
    public Mono<User> getById(Integer id) {
        return repository.findById(id)
                .map(UserEntityMapper::toModel);
    }

    @Override
    public Flux<User> getAll() {
        return repository.findAll()
                .map(UserEntityMapper::toModel);
    }
}
