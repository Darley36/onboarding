package co.com.nequi.r2dbc;

import co.com.nequi.r2dbc.entity.UserEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserRepository extends ReactiveCrudRepository<UserEntity, Integer>,
        ReactiveQueryByExampleExecutor<UserEntity> {

}
