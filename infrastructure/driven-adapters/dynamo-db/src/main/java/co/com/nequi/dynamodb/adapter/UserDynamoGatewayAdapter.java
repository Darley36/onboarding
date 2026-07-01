package co.com.nequi.dynamodb.adapter;

import co.com.nequi.dynamodb.entity.UserEntity;
import co.com.nequi.dynamodb.mapper.UserEntityMapper;
import co.com.nequi.dynamodb.template.DynamoDBTemplateAdapter;
import co.com.nequi.model.enums.ErrorMessage;
import co.com.nequi.model.exception.ApplicationException;
import co.com.nequi.model.user.User;
import co.com.nequi.model.user.gateways.UserDynamoGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UserDynamoGatewayAdapter implements UserDynamoGateway {

    private final DynamoDBTemplateAdapter dynamoDBTemplateAdapter;

    public UserDynamoGatewayAdapter(DynamoDBTemplateAdapter dynamoDBTemplateAdapter) {
        this.dynamoDBTemplateAdapter = dynamoDBTemplateAdapter;
    }

    @Override
    public Mono<User> saveUser(User user) {
        log.info("usuario: "+user);
        return dynamoDBTemplateAdapter.save(user)
                .doOnNext(userSaved -> log.info("Usuario guardado correctamente en Dynamo"))
                .doOnError(throwable -> log.error("Error generico en el listener de la cola SQS - ERROR: " + throwable))
                .onErrorResume(throwable -> Mono.error(new ApplicationException("Error generico de Dynamo - Error:" + throwable.getMessage(), ErrorMessage.ERROR_DYNAMO_DB)));
    }
}
