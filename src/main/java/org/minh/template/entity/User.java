package org.minh.template.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email"}, name = "uk_users_email")
}, indexes = {
        @Index(columnList = "name", name = "idx_users_name"),
        @Index(columnList = "last_name", name = "idx_users_last_name")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends AbstractBaseEntity {
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "birth_date", nullable = false)
    private LocalDateTime birthDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "role_id",
            foreignKey = @ForeignKey(
                    name = "fk_users_role_id",
                    foreignKeyDefinition = "FOREIGN KEY (role_id) REFERENCES roles (id)"
            )
    )
    private Role role;

    /**
     * Get full name of user.
     *
     * @return String
     */
    public String getFullName() {
        return this.lastName + " " + this.name;
    }
}
