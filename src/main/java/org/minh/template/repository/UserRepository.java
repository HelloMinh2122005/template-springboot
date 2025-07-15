package org.minh.template.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.minh.template.entity.User;
import org.minh.template.util.Constants;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @EntityGraph(attributePaths = {"role"})
    Optional<User> findById(@Param("id") UUID id);

    boolean existsByEmailAndIdNot(String email, UUID id);

    @EntityGraph(attributePaths = {"role", "employee", "employee.branch", "orders"})
    List<User> findAllByRoleName(Constants.RoleEnum roleName);

    boolean existsByEmail(@NotBlank(message = "{not_blank}") @Email(message = "{invalid_email}") @Size(max = 100, message = "{max_length}") String email);
}
