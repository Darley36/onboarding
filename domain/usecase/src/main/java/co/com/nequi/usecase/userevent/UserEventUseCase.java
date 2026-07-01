package co.com.nequi.usecase.userevent;

import co.com.nequi.model.user.User;
import co.com.nequi.model.user.gateways.UserDynamoGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserEventUseCase {

    private final UserDynamoGateway userDynamoGateway;

    public Mono<Void> processMessage(User user) {
        return Mono.fromCallable(() ->  getUser(user))
                        .flatMap(userDynamoGateway::saveUser)
                .then();
    }

    private User getUser(User user) {
        return User.builder()
                .id(user.getId())
                .firstName(user.getFirstName().toUpperCase())
                .lastName(user.getLastName().toUpperCase())
                .email(user.getEmail().toUpperCase())
                .avatar(user.getAvatar())
                .build();
    }
}
