package co.com.nequi.r2dbc.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserEntity {
    @Id
    private Integer id;

    @NonNull
    @Column("email")
    private String email;

    @NonNull
    @Column("first_name")
    private String firstName;

    @NonNull
    @Column("last_name")
    private String lastName;

    @NonNull
    @Column("avatar")
    private String avatar;

    @CreatedDate
    @Column("create_date")
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column("last_modified_date")
    private LocalDateTime lastModifiedDate;
}
