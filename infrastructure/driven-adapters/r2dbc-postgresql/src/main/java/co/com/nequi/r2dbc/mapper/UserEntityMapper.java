package co.com.nequi.r2dbc.mapper;

import co.com.nequi.model.user.User;
import co.com.nequi.r2dbc.entity.UserEntity;

public class UserEntityMapper {
    public static UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatar(user.getAvatar())
                .build();
    }

    public static User toModel(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .avatar(entity.getAvatar())
                .build();
    }
}
