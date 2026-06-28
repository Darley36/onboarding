package co.com.nequi.consumer.mapper;

import co.com.nequi.consumer.response.UserResponse;
import co.com.nequi.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResponseMapper {

    public static User toUser(UserResponse userResponse) {
        return new User(
                userResponse.getData().getId(),
                userResponse.getData().getEmail(),
                userResponse.getData().getFirstName(),
                userResponse.getData().getLastName(),
                userResponse.getData().getAvatar()
        );
    }
}
