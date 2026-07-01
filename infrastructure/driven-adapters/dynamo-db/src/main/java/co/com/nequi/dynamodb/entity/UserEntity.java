package co.com.nequi.dynamodb.entity;

import lombok.Builder;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

/* Enhanced DynamoDB annotations are incompatible with Lombok #1932
         https://github.com/aws/aws-sdk-java-v2/issues/1932*/

@DynamoDbBean
public class UserEntity {

    private Integer id;

    private String email;

    private String firstName;

    private String lastName;

    private String avatar;

    public UserEntity() {
    }

    public UserEntity(String avatar, String lastName, String firstName, String email, Integer id) {
        this.avatar = avatar;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.id = id;
    }


    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public Integer getId() {
        return id;
    }

    @DynamoDbAttribute("firstName")
    public String getFirstName() {
        return firstName;
    }

    @DynamoDbAttribute("lastName")
    public String getLastName() {
        return lastName;
    }

    @DynamoDbAttribute("email")
    public String getEmail() {
        return email;
    }

    @DynamoDbAttribute("avatar")
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}
