package co.com.nequi.sqs.listener.helper;

import co.com.nequi.model.user.User;
import co.com.nequi.sqs.listener.SQSProcessor;
import co.com.nequi.sqs.listener.config.SQSProperties;
import co.com.nequi.usecase.userevent.UserEventUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for SQSListener")
class SQSListenerTest {

    @Mock
    private SqsAsyncClient asyncClient;

    @Mock
    private UserEventUseCase userEventUseCase;

    @Mock
    private ObjectMapper objectMapper;

    private SQSProperties sqsProperties;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sqsProperties = new SQSProperties(
                "us-east-1",
                "http://localhost:4566",
                "http://localhost:4566/00000000000/queueName",
                20,
                30,
                10,
                1
        );

        sampleUser = User.builder()
                .id(1)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .avatar("avatar.jpg")
                .build();

        var message = Message.builder().body("{\"id\":1,\"email\":\"test@example.com\"}").build();
        var deleteMessageResponse = DeleteMessageResponse.builder().build();
        var messageResponse = ReceiveMessageResponse.builder().messages(message).build();

        when(asyncClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(messageResponse));
        when(asyncClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(deleteMessageResponse));
    }

    @Test
    @DisplayName("should listen and process messages from SQS queue successfully")
    void listenerTest() throws JsonProcessingException {
        // Arrange
        when(objectMapper.readValue(anyString(), any(Class.class)))
                .thenReturn(sampleUser);
        when(userEventUseCase.processMessage(any(User.class)))
                .thenReturn(Mono.empty());

        var sqsListener = SQSListener.builder()
                .client(asyncClient)
                .properties(sqsProperties)
                .processor(new SQSProcessor(userEventUseCase, objectMapper))
                .operation("operation")
                .build();

        // Act
        Flux<Void> flow = ReflectionTestUtils.invokeMethod(sqsListener, "listen");

        // Assert
        StepVerifier.create(flow).verifyComplete();
    }

    @Test
    @DisplayName("should handle error when processing message fails")
    void shouldHandleErrorWhenProcessingMessageFails() throws JsonProcessingException {
        // Arrange
        when(objectMapper.readValue(anyString(), any(Class.class)))
                .thenThrow(new RuntimeException("JSON parsing error"));

        var sqsListener = SQSListener.builder()
                .client(asyncClient)
                .properties(sqsProperties)
                .processor(new SQSProcessor(userEventUseCase, objectMapper))
                .operation("operation")
                .build();

        // Act
        Flux<Void> flow = ReflectionTestUtils.invokeMethod(sqsListener, "listen");

        // Assert - Should continue despite error (onErrorContinue)
        StepVerifier.create(flow).verifyComplete();
    }

    @Test
    @DisplayName("should handle error from usecase when processing message")
    void shouldHandleErrorFromUsecaseWhenProcessingMessage() throws JsonProcessingException {
        // Arrange
        when(objectMapper.readValue(anyString(), any(Class.class)))
                .thenReturn(sampleUser);
        when(userEventUseCase.processMessage(any(User.class)))
                .thenReturn(Mono.error(new RuntimeException("Usecase processing error")));

        var sqsListener = SQSListener.builder()
                .client(asyncClient)
                .properties(sqsProperties)
                .processor(new SQSProcessor(userEventUseCase, objectMapper))
                .operation("operation")
                .build();

        // Act
        Flux<Void> flow = ReflectionTestUtils.invokeMethod(sqsListener, "listen");

        // Assert - Should continue despite error (onErrorContinue)
        StepVerifier.create(flow).verifyComplete();
    }
}
