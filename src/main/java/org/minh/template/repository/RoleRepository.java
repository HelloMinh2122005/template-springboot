package org.minh.template.repository;

import org.minh.template.entity.Role;
import org.minh.template.util.Constants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(Constants.RoleEnum name);
}
