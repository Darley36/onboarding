package co.com.nequi.api;

import co.com.nequi.api.handler.UserHandler;
import co.com.nequi.api.exception.ErrorHandler;
import co.com.nequi.model.user.User;
import co.com.nequi.usecase.user.UserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for UserHandler")
class RouterRestTest {

    @Mock
    private UserUseCase userUseCase;

    @Mock
    private ErrorHandler errorHandler;

    @Mock
    private ServerRequest serverRequest;

    @InjectMocks
    private UserHandler userHandler;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .avatar("avatar.png")
                .build();
    }

    @Test
    @DisplayName("it must save a user successfully")
    void shouldSaveUserSuccessfully() {
        // Arrange
        when(serverRequest.pathVariable("id")).thenReturn("1");
        when(userUseCase.saveUserById(eq(1))).thenReturn(Mono.just(sampleUser));

        // Act
        Mono<ServerResponse> result = userHandler.saveUser(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.OK)
                .verifyComplete();

        verify(userUseCase, times(1)).saveUserById(eq(1));
    }

    @Test
    @DisplayName("it must handle error when saving user with invalid id")
    void shouldHandleErrorWhenSavingUserWithInvalidId() {
        // Arrange
        when(serverRequest.pathVariable("id")).thenReturn("invalid");
        when(errorHandler.handleError(any(), any()))
                .thenReturn(ServerResponse.status(HttpStatus.BAD_REQUEST).build());

        // Act
        Mono<ServerResponse> result = userHandler.saveUser(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();

        verify(userUseCase, never()).saveUserById(anyInt());
    }

    @Test
    @DisplayName("it must get user by id successfully")
    void shouldGetUserByIdSuccessfully() {
        // Arrange
        when(serverRequest.queryParam("id")).thenReturn(java.util.Optional.of("1"));
        when(userUseCase.getById(eq(1))).thenReturn(Mono.just(sampleUser));

        // Act
        Mono<ServerResponse> result = userHandler.getById(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.OK)
                .verifyComplete();

        verify(userUseCase, times(1)).getById(eq(1));
    }

    @Test
    @DisplayName("it must handle error when getting user with missing id parameter")
    void shouldHandleErrorWhenGettingUserWithMissingId() {
        // Arrange
        when(serverRequest.queryParam("id")).thenReturn(java.util.Optional.empty());
        when(errorHandler.handleError(any(), any()))
                .thenReturn(ServerResponse.status(HttpStatus.BAD_REQUEST).build());

        // Act
        Mono<ServerResponse> result = userHandler.getById(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();

        verify(userUseCase, never()).getById(anyInt());
    }

    @Test
    @DisplayName("it must get all users successfully")
    void shouldGetAllUsersSuccessfully() {
        // Arrange
        when(serverRequest.queryParam("tamano")).thenReturn(java.util.Optional.of("10"));
        when(serverRequest.queryParam("pagina")).thenReturn(java.util.Optional.of("0"));
        when(userUseCase.getAll(eq(10), eq(0))).thenReturn(Flux.just(sampleUser));

        // Act
        Mono<ServerResponse> result = userHandler.getAllUser(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.OK)
                .verifyComplete();

        verify(userUseCase, times(1)).getAll(eq(10), eq(0));
    }

    @Test
    @DisplayName("it must handle error when getting all users with invalid parameters")
    void shouldHandleErrorWhenGettingAllUsersWithInvalidParameters() {
        // Arrange
        when(serverRequest.queryParam("tamano")).thenReturn(java.util.Optional.of("invalid"));
        when(serverRequest.queryParam("pagina")).thenReturn(java.util.Optional.of("0"));
        when(errorHandler.handleError(any(), any()))
                .thenReturn(ServerResponse.status(HttpStatus.BAD_REQUEST).build());

        // Act
        Mono<ServerResponse> result = userHandler.getAllUser(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();

        verify(userUseCase, never()).getAll(anyInt(), anyInt());
    }

    @Test
    @DisplayName("it must get users by name successfully")
    void shouldGetUsersByNameSuccessfully() {
        // Arrange
        when(serverRequest.queryParam("name")).thenReturn(java.util.Optional.of("John"));
        when(userUseCase.getByName(eq("John"))).thenReturn(Flux.just(sampleUser));

        // Act
        Mono<ServerResponse> result = userHandler.getByName(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.OK)
                .verifyComplete();

        verify(userUseCase, times(1)).getByName(eq("John"));
    }

    @Test
    @DisplayName("it must handle error when getting users with missing name parameter")
    void shouldHandleErrorWhenGettingUsersByNameWithMissingParameter() {
        // Arrange
        when(serverRequest.queryParam("name")).thenReturn(java.util.Optional.empty());
        when(errorHandler.handleError(any(), any()))
                .thenReturn(ServerResponse.status(HttpStatus.BAD_REQUEST).build());

        // Act
        Mono<ServerResponse> result = userHandler.getByName(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();

        verify(userUseCase, never()).getByName(anyString());
    }

    @Test
    @DisplayName("it must handle exception when usecase fails")
    void shouldHandleExceptionWhenUsecaseFails() {
        // Arrange
        when(serverRequest.pathVariable("id")).thenReturn("1");
        when(userUseCase.saveUserById(eq(1))).thenReturn(Mono.error(new RuntimeException("Database error")));
        when(errorHandler.handleError(any(), any()))
                .thenReturn(ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        // Act
        Mono<ServerResponse> result = userHandler.saveUser(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                .verifyComplete();

        verify(errorHandler, times(1)).handleError(any(), any());
    }
}
