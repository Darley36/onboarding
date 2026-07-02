package co.com.nequi.sqs.sender;

import co.com.nequi.model.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for SQSAdapter")
class SQSAdapterTest {

    @Mock
    private SQSSender sqsSender;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SQSAdapter sqsAdapter;

    private User sampleUser;
    private String userJson;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .avatar("avatar.jpg")
                .build();

        userJson = "{\"id\":1,\"email\":\"test@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"avatar\":\"avatar.jpg\"}";
    }

    @Test
    @DisplayName("should send user event successfully to SQS")
    void shouldSendUserEventSuccessfully() throws JsonProcessingException {
        // Arrange
        when(objectMapper.writeValueAsString(any(User.class)))
                .thenReturn(userJson);
        when(sqsSender.send(anyString()))
                .thenReturn(Mono.just("message-id-12345"));

        // Act
        Mono<Void> result = sqsAdapter.sendUserEvent(sampleUser);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(objectMapper, times(1)).writeValueAsString(any(User.class));
        verify(sqsSender, times(1)).send(eq(userJson));
    }

    @Test
    @DisplayName("should handle error when ObjectMapper fails to serialize user")
    void shouldHandleErrorWhenObjectMapperFails() throws JsonProcessingException {
        // Arrange
        when(objectMapper.writeValueAsString(any(User.class)))
                .thenThrow(new RuntimeException("JSON serialization error"));

        // Act
        Mono<Void> result = sqsAdapter.sendUserEvent(sampleUser);

        // Assert - Should return Mono.empty() despite error (onErrorResume)
        StepVerifier.create(result)
                .verifyComplete();

        verify(objectMapper, times(1)).writeValueAsString(any(User.class));
        verify(sqsSender, never()).send(anyString());
    }

    @Test
    @DisplayName("should handle error when SQSSender fails to send message")
    void shouldHandleErrorWhenSQSSenderFails() throws JsonProcessingException {
        // Arrange
        when(objectMapper.writeValueAsString(any(User.class)))
                .thenReturn(userJson);
        when(sqsSender.send(anyString()))
                .thenReturn(Mono.error(new RuntimeException("SQS connection error")));

        // Act
        Mono<Void> result = sqsAdapter.sendUserEvent(sampleUser);

        // Assert - Should return Mono.empty() despite error (onErrorResume)
        StepVerifier.create(result)
                .verifyComplete();

        verify(objectMapper, times(1)).writeValueAsString(any(User.class));
        verify(sqsSender, times(1)).send(eq(userJson));
    }

    @Test
    @DisplayName("should send event with complete user data")
    void shouldSendEventWithCompleteUserData() throws JsonProcessingException {
        // Arrange
        User completeUser = User.builder()
                .id(999)
                .email("john.doe@company.com")
                .firstName("John")
                .lastName("Doe")
                .avatar("https://example.com/avatar.jpg")
                .build();

        String completeUserJson = "{\"id\":999,\"email\":\"john.doe@company.com\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"avatar\":\"https://example.com/avatar.jpg\"}";

        when(objectMapper.writeValueAsString(any(User.class)))
                .thenReturn(completeUserJson);
        when(sqsSender.send(anyString()))
                .thenReturn(Mono.just("message-id-67890"));

        // Act
        Mono<Void> result = sqsAdapter.sendUserEvent(completeUser);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(objectMapper, times(1)).writeValueAsString(eq(completeUser));
        verify(sqsSender, times(1)).send(eq(completeUserJson));
    }

    @Test
    @DisplayName("should handle null fields in user object gracefully")
    void shouldHandleNullFieldsInUserObject() throws JsonProcessingException {
        // Arrange
        User userWithNulls = User.builder()
                .id(2)
                .email("test2@example.com")
                .firstName(null)
                .lastName(null)
                .avatar(null)
                .build();

        String userWithNullsJson = "{\"id\":2,\"email\":\"test2@example.com\"}";

        when(objectMapper.writeValueAsString(any(User.class)))
                .thenReturn(userWithNullsJson);
        when(sqsSender.send(anyString()))
                .thenReturn(Mono.just("message-id-11111"));

        // Act
        Mono<Void> result = sqsAdapter.sendUserEvent(userWithNulls);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(objectMapper, times(1)).writeValueAsString(eq(userWithNulls));
        verify(sqsSender, times(1)).send(eq(userWithNullsJson));
    }

    @Test
    @DisplayName("should send multiple user events sequentially")
    void shouldSendMultipleUserEventsSequentially() throws JsonProcessingException {
        // Arrange
        User user1 = User.builder().id(1).email("user1@example.com").firstName("User").lastName("One").build();
        User user2 = User.builder().id(2).email("user2@example.com").firstName("User").lastName("Two").build();

        String user1Json = "{\"id\":1,\"email\":\"user1@example.com\"}";
        String user2Json = "{\"id\":2,\"email\":\"user2@example.com\"}";

        when(objectMapper.writeValueAsString(eq(user1)))
                .thenReturn(user1Json);
        when(objectMapper.writeValueAsString(eq(user2)))
                .thenReturn(user2Json);
        when(sqsSender.send(anyString()))
                .thenReturn(Mono.just("message-id-1"))
                .thenReturn(Mono.just("message-id-2"));

        // Act & Assert
        StepVerifier.create(sqsAdapter.sendUserEvent(user1))
                .verifyComplete();

        StepVerifier.create(sqsAdapter.sendUserEvent(user2))
                .verifyComplete();

        verify(objectMapper, times(1)).writeValueAsString(eq(user1));
        verify(objectMapper, times(1)).writeValueAsString(eq(user2));
        verify(sqsSender, times(1)).send(eq(user1Json));
        verify(sqsSender, times(1)).send(eq(user2Json));
    }
}

