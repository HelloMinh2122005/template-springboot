package org.minh.template.service.user.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.minh.template.dto.request.auth.RegisterRequest;
import org.minh.template.dto.request.user.UpdateUserRequest;
import org.minh.template.entity.User;
import org.minh.template.exception.NotFoundException;
import org.minh.template.repository.UserRepository;
import org.minh.template.security.JwtUserDetails;
import org.minh.template.service.MessageSourceService;
import org.minh.template.service.role.RoleService;
import org.minh.template.service.user.UserService;
import org.minh.template.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final MessageSourceService messageSourceService;

    private final RoleService roleService;

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser() {
        Authentication authentication = getAuthentication();
        if (authentication.isAuthenticated()) {
            try {
                return findById(UUID.fromString(getPrincipal(authentication).getId()));
            } catch (ClassCastException e) {
                log.warn("[JWT] User details not found!");
                throw new BadCredentialsException("Bad credentials");
            }
        } else {
            log.warn("[JWT] User not authenticated!");
            throw new BadCredentialsException("Bad credentials");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    /**
     * Find a user by email.
     *
     * @param email String.
     * @return User
     */
    @Transactional(readOnly = true)
    public User findByEmail(final String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(messageSourceService.get("not_found_with_param",
                        new String[]{messageSourceService.get("user")})));
    }

    /**
     * Load user details by username.
     *
     * @param email String
     * @return UserDetails
     * @throws UsernameNotFoundException email not found exception.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(final String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(messageSourceService.get("not_found_with_param",
                        new String[]{messageSourceService.get("user")})));

        return JwtUserDetails.create(user);
    }

    /**
     * Loads user details by UUID string.
     *
     * @param id String
     * @return UserDetails
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserById(final String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException(messageSourceService.get("not_found_with_param",
                        new String[]{messageSourceService.get("user")})));

        return JwtUserDetails.create(user);
    }

    /**
     * Get UserDetails from security context.
     *
     * @param authentication Wrapper for security context
     * @return the Principal being authenticated or the authenticated principal after authentication.
     */
    @Override
    @Transactional(readOnly = true)
    public JwtUserDetails getPrincipal(final Authentication authentication) {
        return (JwtUserDetails) authentication.getPrincipal();
    }

    @Transactional
    @Override
    public User register(final RegisterRequest request) throws BindException {

        if (userRepository.existsByEmail(request.getEmail())) {
            BindingResult bindingResult = new BeanPropertyBindingResult(request, "request");
            bindingResult.addError(new FieldError(bindingResult.getObjectName(), "email",
                    "Email already exists"));
            throw new BindException(bindingResult);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setLastName(request.getLastName());
        user.setBirthDate(request.getBirthDate());
        user.setGender(request.getGender());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(roleService.findByName(Constants.RoleEnum.USER));
        userRepository.save(user);

        return user;
    }

    @Transactional
    @Override
    public User updateMe(UpdateUserRequest request) throws BindException {
        User user = getUser();
        user.setBirthDate(request.getBirthDate());
        user.setGender(request.getGender());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setName(request.getName());
        user.setLastName(request.getLastName());
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public void delete(String id) {
        userRepository.delete(findById(UUID.fromString(id)));
    }
}
