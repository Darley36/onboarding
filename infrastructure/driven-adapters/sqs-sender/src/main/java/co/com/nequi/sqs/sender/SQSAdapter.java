package co.com.nequi.sqs.sender;

import co.com.nequi.model.user.User;
import co.com.nequi.model.user.gateways.UserEventGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SQSAdapter implements UserEventGateway {
    private final SQSSender sqsSender;
    private final ObjectMapper objectMapper;

    public SQSAdapter(SQSSender sqsSender, @Qualifier("jacksonObjectMapper") ObjectMapper objectMapper) {
        this.sqsSender = sqsSender;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> sendUserEvent(User user) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(user))
                .flatMap(sqsSender::send)
                .doOnSuccess(unused -> log.info("Evento enviado correctamente al SQS"))
                .then()
                .onErrorResume(throwable -> {
                    log.warn("Error al enviar el evento al SQS, error :"+throwable.getMessage());
                    return Mono.empty();
                });

    }
}