package co.com.nequi.r2dbc;

import co.com.nequi.model.exception.ApplicationException;
import co.com.nequi.model.exception.BusinessException;
import co.com.nequi.model.user.User;
import co.com.nequi.r2dbc.adapter.UserRepositoryAdapter;
import co.com.nequi.r2dbc.entity.UserEntity;
import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for UserRepositoryAdapter")
class UserRepositoryAdapterTest {

    @Mock
    private UserRepository repository;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private R2dbcEntityTemplate entityTemplate;

    private UserRepositoryAdapter repositoryAdapter;
    private User sampleUser;
    private UserEntity sampleUserEntity;

    @BeforeEach
    void setUp() {
        repositoryAdapter = new UserRepositoryAdapter(repository, mapper, entityTemplate);

        sampleUser = User.builder()
                .id(1)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .avatar("avatar.jpg")
                .build();

        sampleUserEntity = UserEntity.builder()
                .id(1)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .avatar("avatar.jpg")
                .build();
    }

    // ====== SAVE USER TESTS ======

    @Test
    @DisplayName("should save user successfully")
    void shouldSaveUserSuccessfully() {
        // Arrange
        when(mapper.map(eq(sampleUser), eq(UserEntity.class)))
                .thenReturn(sampleUserEntity);
        when(entityTemplate.insert(eq(sampleUserEntity)))
                .thenReturn(Mono.just(sampleUserEntity));
        when(mapper.map(eq(sampleUserEntity), eq(User.class)))
                .thenReturn(sampleUser);

        // Act
        Mono<User> result = repositoryAdapter.saveUser(sampleUser);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(user -> user.getId().equals(1) && user.getEmail().equals("test@example.com"))
                .verifyComplete();

        verify(mapper, times(1)).map(eq(sampleUser), eq(UserEntity.class));
        verify(entityTemplate, times(1)).insert(eq(sampleUserEntity));
    }

    @Test
    @DisplayName("should handle DataIntegrityViolationException when saving duplicate user")
    void shouldHandleDataIntegrityViolationException() {
        // Arrange
        when(mapper.map(eq(sampleUser), eq(UserEntity.class)))
                .thenReturn(sampleUserEntity);
        when(entityTemplate.insert(eq(sampleUserEntity)))
                .thenReturn(Mono.error(new DataIntegrityViolationException("Duplicate key")));

        // Act
        Mono<User> result = repositoryAdapter.saveUser(sampleUser);

        // Assert
        StepVerifier.create(result)
                .expectError(BusinessException.class)
                .verify();

        verify(entityTemplate, times(1)).insert(eq(sampleUserEntity));
    }

    @Test
    @DisplayName("should handle R2dbcDataIntegrityViolationException when saving duplicate user")
    void shouldHandleR2dbcDataIntegrityViolationException() {
        // Arrange
        when(mapper.map(eq(sampleUser), eq(UserEntity.class)))
                .thenReturn(sampleUserEntity);
        when(entityTemplate.insert(eq(sampleUserEntity)))
                .thenReturn(Mono.error(new R2dbcDataIntegrityViolationException("Duplicate key", "23505", null)));

        // Act
        Mono<User> result = repositoryAdapter.saveUser(sampleUser);

        // Assert
        StepVerifier.create(result)
                .expectError(BusinessException.class)
                .verify();

        verify(entityTemplate, times(1)).insert(eq(sampleUserEntity));
    }

    @Test
    @DisplayName("should handle generic exception when saving user")
    void shouldHandleGenericExceptionWhenSavingUser() {
        // Arrange
        when(mapper.map(eq(sampleUser), eq(UserEntity.class)))
                .thenReturn(sampleUserEntity);
        when(entityTemplate.insert(eq(sampleUserEntity)))
                .thenReturn(Mono.error(new RuntimeException("Unexpected error")));

        // Act
        Mono<User> result = repositoryAdapter.saveUser(sampleUser);

        // Assert
        StepVerifier.create(result)
                .expectError(ApplicationException.class)
                .verify();

        verify(entityTemplate, times(1)).insert(eq(sampleUserEntity));
    }

    // ====== GET BY ID TESTS ======

    @Test
    @DisplayName("should get user by id successfully")
    void shouldGetUserByIdSuccessfully() {
        // Arrange
        when(repository.findById(eq(1)))
                .thenReturn(Mono.just(sampleUserEntity));

        // Act
        Mono<User> result = repositoryAdapter.getById(1);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(user -> user.getId().equals(1))
                .verifyComplete();

        verify(repository, times(1)).findById(eq(1));
    }

    @Test
    @DisplayName("should return empty when user not found by id")
    void shouldReturnEmptyWhenUserNotFoundById() {
        // Arrange
        when(repository.findById(eq(999)))
                .thenReturn(Mono.empty());

        // Act
        Mono<User> result = repositoryAdapter.getById(999);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(repository, times(1)).findById(eq(999));
    }

}
