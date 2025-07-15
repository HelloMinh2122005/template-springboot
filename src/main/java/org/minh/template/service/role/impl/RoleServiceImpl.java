package org.minh.template.service.role.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.minh.template.entity.Role;
import org.minh.template.repository.RoleRepository;
import org.minh.template.service.role.RoleService;
import org.minh.template.util.Constants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public Role findByName(final Constants.RoleEnum name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Role not found " + name));
    }

    @Override
    @Transactional
    public Role create(final Role role) {
        return roleRepository.save(role);
    }
}
