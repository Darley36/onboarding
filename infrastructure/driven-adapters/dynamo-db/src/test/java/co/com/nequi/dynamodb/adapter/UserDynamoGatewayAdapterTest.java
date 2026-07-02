package co.com.nequi.dynamodb.adapter;

import co.com.nequi.model.enums.ErrorMessage;
import co.com.nequi.model.exception.ApplicationException;
import co.com.nequi.model.user.User;
import co.com.nequi.dynamodb.template.DynamoDBTemplateAdapter;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for UserDynamoGatewayAdapter")
class UserDynamoGatewayAdapterTest {

    @Mock
    private DynamoDBTemplateAdapter dynamoDBTemplateAdapter;

    @InjectMocks
    private UserDynamoGatewayAdapter userDynamoGatewayAdapter;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .avatar("avatar.jpg")
                .build();
    }

    @Test
    @DisplayName("should save user to DynamoDB successfully")
    void shouldSaveUserSuccessfully() {
        // Arrange
        when(dynamoDBTemplateAdapter.save(any(User.class)))
                .thenReturn(Mono.just(sampleUser));

        // Act
        Mono<User> result = userDynamoGatewayAdapter.saveUser(sampleUser);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(user -> user.getId().equals(1) && user.getEmail().equals("test@example.com"))
                .verifyComplete();

        verify(dynamoDBTemplateAdapter, times(1)).save(eq(sampleUser));
    }

    @Test
    @DisplayName("should handle error when saving user to DynamoDB fails")
    void shouldHandleErrorWhenSavingUserFails() {
        // Arrange
        when(dynamoDBTemplateAdapter.save(any(User.class)))
                .thenReturn(Mono.error(new RuntimeException("DynamoDB connection error")));

        // Act
        Mono<User> result = userDynamoGatewayAdapter.saveUser(sampleUser);

        // Assert
        StepVerifier.create(result)
                .expectError(ApplicationException.class)
                .verify();

        verify(dynamoDBTemplateAdapter, times(1)).save(eq(sampleUser));
    }

    @Test
    @DisplayName("should handle null pointer exception when saving user")
    void shouldHandleNullPointerExceptionWhenSavingUser() {
        // Arrange
        when(dynamoDBTemplateAdapter.save(any(User.class)))
                .thenReturn(Mono.error(new NullPointerException("User object is null")));

        // Act
        Mono<User> result = userDynamoGatewayAdapter.saveUser(sampleUser);

        // Assert
        StepVerifier.create(result)
                .expectError(ApplicationException.class)
                .verify();

        verify(dynamoDBTemplateAdapter, times(1)).save(eq(sampleUser));
    }

    @Test
    @DisplayName("should save user with complete data")
    void shouldSaveUserWithCompleteData() {
        // Arrange
        User completeUser = User.builder()
                .id(999)
                .email("john.doe@company.com")
                .firstName("John")
                .lastName("Doe")
                .avatar("https://example.com/avatar.jpg")
                .build();

        when(dynamoDBTemplateAdapter.save(eq(completeUser)))
                .thenReturn(Mono.just(completeUser));

        // Act
        Mono<User> result = userDynamoGatewayAdapter.saveUser(completeUser);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(user ->
                    user.getId().equals(999) &&
                    user.getEmail().equals("john.doe@company.com") &&
                    user.getFirstName().equals("John") &&
                    user.getLastName().equals("Doe")
                )
                .verifyComplete();

        verify(dynamoDBTemplateAdapter, times(1)).save(eq(completeUser));
    }

    @Test
    @DisplayName("should save user with null optional fields")
    void shouldSaveUserWithNullOptionalFields() {
        // Arrange
        User userWithNulls = User.builder()
                .id(2)
                .email("minimal@example.com")
                .firstName(null)
                .lastName(null)
                .avatar(null)
                .build();

        when(dynamoDBTemplateAdapter.save(eq(userWithNulls)))
                .thenReturn(Mono.just(userWithNulls));

        // Act
        Mono<User> result = userDynamoGatewayAdapter.saveUser(userWithNulls);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(user -> user.getId().equals(2) && user.getEmail().equals("minimal@example.com"))
                .verifyComplete();

        verify(dynamoDBTemplateAdapter, times(1)).save(eq(userWithNulls));
    }

    @Test
    @DisplayName("should handle timeout error from DynamoDB")
    void shouldHandleTimeoutErrorFromDynamoDB() {
        // Arrange
        when(dynamoDBTemplateAdapter.save(any(User.class)))
                .thenReturn(Mono.error(new RuntimeException("DynamoDB request timeout")));

        // Act
        Mono<User> result = userDynamoGatewayAdapter.saveUser(sampleUser);

        // Assert
        StepVerifier.create(result)
                .expectError(ApplicationException.class)
                .verify();

        verify(dynamoDBTemplateAdapter, times(1)).save(eq(sampleUser));
    }

    @Test
    @DisplayName("should save multiple users sequentially")
    void shouldSaveMultipleUsersSequentially() {
        // Arrange
        User user1 = User.builder().id(1).email("user1@example.com").firstName("User").lastName("One").build();
        User user2 = User.builder().id(2).email("user2@example.com").firstName("User").lastName("Two").build();

        when(dynamoDBTemplateAdapter.save(eq(user1)))
                .thenReturn(Mono.just(user1));
        when(dynamoDBTemplateAdapter.save(eq(user2)))
                .thenReturn(Mono.just(user2));

        // Act & Assert
        StepVerifier.create(userDynamoGatewayAdapter.saveUser(user1))
                .expectNextMatches(user -> user.getId().equals(1))
                .verifyComplete();

        StepVerifier.create(userDynamoGatewayAdapter.saveUser(user2))
                .expectNextMatches(user -> user.getId().equals(2))
                .verifyComplete();

        verify(dynamoDBTemplateAdapter, times(1)).save(eq(user1));
        verify(dynamoDBTemplateAdapter, times(1)).save(eq(user2));
    }
}

