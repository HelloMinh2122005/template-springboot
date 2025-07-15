package org.minh.template.service.dummydata.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.minh.template.entity.Role;
import org.minh.template.entity.User;
import org.minh.template.repository.RoleRepository;
import org.minh.template.repository.UserRepository;
import org.minh.template.util.Constants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserBasicInfo {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public void create() {
        createRole();
        createUser();
    }

    private void createRole() {
        List<Role> roleList = new ArrayList<>();
        roleList.add(Role.builder().name(Constants.RoleEnum.ADMIN).build());
        roleList.add(Role.builder().name(Constants.RoleEnum.USER).build());

        roleRepository.saveAll(roleList);
    }

    private void createUser() {
        String defaultPassword = "P@sswd123.";

    }
}
