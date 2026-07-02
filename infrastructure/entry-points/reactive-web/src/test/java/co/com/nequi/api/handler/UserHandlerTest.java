package co.com.nequi.api.handler;

import co.com.nequi.api.exception.ErrorHandler;
import co.com.nequi.model.enums.ErrorMessage;
import co.com.nequi.model.exception.BusinessException;
import co.com.nequi.model.exception.ValidationException;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for UserHandler")
class UserHandlerTest {

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
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .avatar("avatar.jpg")
                .build();
    }

    // ====== SAVE USER TESTS ======

    @Test
    @DisplayName("should save user successfully with valid id")
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
    @DisplayName("should handle error when saving user with non-numeric id")
    void shouldHandleErrorWhenSavingUserWithNonNumericId() {
        // Arrange
        when(serverRequest.pathVariable("id")).thenReturn("abc");
        when(errorHandler.handleError(any(ValidationException.class), eq(serverRequest)))
                .thenReturn(ServerResponse.status(HttpStatus.BAD_REQUEST).build());

        // Act
        Mono<ServerResponse> result = userHandler.saveUser(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();

        verify(userUseCase, never()).saveUserById(anyInt());
        verify(errorHandler, times(1)).handleError(any(ValidationException.class), eq(serverRequest));
    }

    @Test
    @DisplayName("should handle error when saving user with blank id")
    void shouldHandleErrorWhenSavingUserWithBlankId() {
        // Arrange
        when(serverRequest.pathVariable("id")).thenReturn("");
        when(errorHandler.handleError(any(ValidationException.class), eq(serverRequest)))
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
    @DisplayName("should handle business exception when saving user")
    void shouldHandleBusinessExceptionWhenSavingUser() {
        // Arrange
        BusinessException exception = new BusinessException(
                "El usuario proporcionado ya existe en el sistema.",
                ErrorMessage.ERROR_POSTGRES_DUPLICATE
        );

        when(serverRequest.pathVariable("id")).thenReturn("1");
        when(userUseCase.saveUserById(eq(1))).thenReturn(Mono.error(exception));
        when(errorHandler.handleError(any(BusinessException.class), eq(serverRequest)))
                .thenReturn(ServerResponse.status(HttpStatus.CONFLICT).build());

        // Act
        Mono<ServerResponse> result = userHandler.saveUser(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.CONFLICT)
                .verifyComplete();

        verify(errorHandler, times(1)).handleError(any(BusinessException.class), eq(serverRequest));
    }

    // ====== GET BY ID TESTS ======

    @Test
    @DisplayName("should get user by id successfully")
    void shouldGetUserByIdSuccessfully() {
        // Arrange
        when(serverRequest.queryParam("id")).thenReturn(Optional.of("1"));
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
    @DisplayName("should handle error when getting user with missing id parameter")
    void shouldHandleErrorWhenGettingUserWithMissingId() {
        // Arrange
        when(serverRequest.queryParam("id")).thenReturn(Optional.empty());
        when(errorHandler.handleError(any(ValidationException.class), eq(serverRequest)))
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
    @DisplayName("should handle error when getting user with non-numeric id")
    void shouldHandleErrorWhenGettingUserWithNonNumericId() {
        // Arrange
        when(serverRequest.queryParam("id")).thenReturn(Optional.of("abc"));
        when(errorHandler.handleError(any(ValidationException.class), eq(serverRequest)))
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
    @DisplayName("should handle not found exception when getting user")
    void shouldHandleNotFoundExceptionWhenGettingUser() {
        // Arrange
        BusinessException exception = new BusinessException(
                "El usuario no fue encontrado",
                ErrorMessage.ERROR_POSTGRES_NOT_FOUND
        );

        when(serverRequest.queryParam("id")).thenReturn(Optional.of("1"));
        when(userUseCase.getById(eq(1))).thenReturn(Mono.error(exception));
        when(errorHandler.handleError(any(BusinessException.class), eq(serverRequest)))
                .thenReturn(ServerResponse.status(HttpStatus.NOT_FOUND).build());

        // Act
        Mono<ServerResponse> result = userHandler.getById(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(errorHandler, times(1)).handleError(any(BusinessException.class), eq(serverRequest));
    }

    // ====== GET ALL USER TESTS ======

    @Test
    @DisplayName("should get all users successfully with valid pagination parameters")
    void shouldGetAllUsersSuccessfully() {
        // Arrange
        when(serverRequest.queryParam("tamano")).thenReturn(Optional.of("10"));
        when(serverRequest.queryParam("pagina")).thenReturn(Optional.of("0"));
        when(userUseCase.getAll(eq(10), eq(0))).thenReturn(Flux.just(sampleUser, sampleUser));

        // Act
        Mono<ServerResponse> result = userHandler.getAllUser(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.OK)
                .verifyComplete();

        verify(userUseCase, times(1)).getAll(eq(10), eq(0));
    }

    @Test
    @DisplayName("should handle error when getting all users with missing tamano parameter")
    void shouldHandleErrorWhenGettingAllUsersWithMissingTamano() {
        // Arrange
        when(serverRequest.queryParam("tamano")).thenReturn(Optional.empty());
        when(serverRequest.queryParam("pagina")).thenReturn(Optional.of("0"));
        when(errorHandler.handleError(any(ValidationException.class), eq(serverRequest)))
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
    @DisplayName("should handle error when getting all users with invalid tamano parameter")
    void shouldHandleErrorWhenGettingAllUsersWithInvalidTamano() {
        // Arrange
        when(serverRequest.queryParam("tamano")).thenReturn(Optional.of("invalid"));
        when(serverRequest.queryParam("pagina")).thenReturn(Optional.of("0"));
        when(errorHandler.handleError(any(ValidationException.class), eq(serverRequest)))
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
    @DisplayName("should handle error when getting all users with missing pagina parameter")
    void shouldHandleErrorWhenGettingAllUsersWithMissingPagina() {
        // Arrange
        when(serverRequest.queryParam("tamano")).thenReturn(Optional.of("10"));
        when(serverRequest.queryParam("pagina")).thenReturn(Optional.empty());
        when(errorHandler.handleError(any(ValidationException.class), eq(serverRequest)))
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
    @DisplayName("should handle error when getting all users with invalid pagina parameter")
    void shouldHandleErrorWhenGettingAllUsersWithInvalidPagina() {
        // Arrange
        when(serverRequest.queryParam("tamano")).thenReturn(Optional.of("10"));
        when(serverRequest.queryParam("pagina")).thenReturn(Optional.of("abc"));
        when(errorHandler.handleError(any(ValidationException.class), eq(serverRequest)))
                .thenReturn(ServerResponse.status(HttpStatus.BAD_REQUEST).build());

        // Act
        Mono<ServerResponse> result = userHandler.getAllUser(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();

        verify(userUseCase, never()).getAll(anyInt(), anyInt());
    }

    // ====== GET BY NAME TESTS ======

    @Test
    @DisplayName("should get users by name successfully")
    void shouldGetUsersByNameSuccessfully() {
        // Arrange
        when(serverRequest.queryParam("name")).thenReturn(Optional.of("John"));
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
    @DisplayName("should handle error when getting users with missing name parameter")
    void shouldHandleErrorWhenGettingUsersWithMissingName() {
        // Arrange
        when(serverRequest.queryParam("name")).thenReturn(Optional.empty());
        when(errorHandler.handleError(any(ValidationException.class), eq(serverRequest)))
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
    @DisplayName("should handle error when getting users with blank name parameter")
    void shouldHandleErrorWhenGettingUsersWithBlankName() {
        // Arrange
        when(serverRequest.queryParam("name")).thenReturn(Optional.of("   "));
        when(errorHandler.handleError(any(ValidationException.class), eq(serverRequest)))
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
    @DisplayName("should handle not found exception when getting users by name")
    void shouldHandleNotFoundExceptionWhenGettingUsersByName() {
        // Arrange
        BusinessException exception = new BusinessException(
                "No existe ningun usuario con ese nombre",
                ErrorMessage.ERROR_POSTGRES_NOT_FOUND
        );

        when(serverRequest.queryParam("name")).thenReturn(Optional.of("NonExistent"));
        when(userUseCase.getByName(eq("NonExistent"))).thenReturn(Flux.error(exception));
        when(errorHandler.handleError(any(BusinessException.class), eq(serverRequest)))
                .thenReturn(ServerResponse.status(HttpStatus.NOT_FOUND).build());

        // Act
        Mono<ServerResponse> result = userHandler.getByName(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();

        verify(errorHandler, times(1)).handleError(any(BusinessException.class), eq(serverRequest));
    }

    @Test
    @DisplayName("should get multiple users by name successfully")
    void shouldGetMultipleUsersByNameSuccessfully() {
        // Arrange
        User user2 = User.builder()
                .id(2)
                .email("john.smith@example.com")
                .firstName("John")
                .lastName("Smith")
                .avatar("avatar2.jpg")
                .build();

        when(serverRequest.queryParam("name")).thenReturn(Optional.of("John"));
        when(userUseCase.getByName(eq("John"))).thenReturn(Flux.just(sampleUser, user2));

        // Act
        Mono<ServerResponse> result = userHandler.getByName(serverRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.statusCode() == HttpStatus.OK)
                .verifyComplete();

        verify(userUseCase, times(1)).getByName(eq("John"));
    }
}

