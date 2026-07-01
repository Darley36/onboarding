package co.com.nequi.sqs.listener;

import co.com.nequi.model.user.User;
import co.com.nequi.usecase.userevent.UserEventUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {
    private final UserEventUseCase userEventUseCase;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> apply(Message message) {
        log.info("Evento recibido"+message);
        return Mono.fromCallable(() -> objectMapper.readValue(message.body(), User.class))
                .flatMap(userEventUseCase::processMessage)
                .onErrorResume(throwable -> {
                    log.error("Error processing message: {}", throwable.getMessage());
                    return Mono.empty();
                });
    }
}
