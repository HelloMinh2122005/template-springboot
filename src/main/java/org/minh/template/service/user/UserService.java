package org.minh.template.service.user;

import org.minh.template.dto.request.auth.RegisterRequest;
import org.minh.template.dto.request.user.UpdateUserRequest;
import org.minh.template.entity.User;
import org.minh.template.security.JwtUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindException;

import java.util.UUID;

public interface UserService {
    User getUser();

    UserDetails loadUserById(String id);

    JwtUserDetails getPrincipal(Authentication authentication);

    Page<User> findAll(Pageable pageable);

    User findById(UUID id);

    User findByEmail(String email);

    UserDetails loadUserByEmail(String email);

    User register(RegisterRequest request) throws BindException;

    User updateMe(UpdateUserRequest request) throws BindException;

    void delete(String id);
}