package org.minh.template.service.dummydata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.minh.template.repository.RoleRepository;
import org.minh.template.service.dummydata.account.UserBasicInfo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MainDummyData implements CommandLineRunner {
    private final UserBasicInfo userBasicInfo;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        // check if the database already exists
        if (roleRepository.count() > 0) {
            log.info("Database already exists, skipping dummy data creation.");
            return;
        }

        // Initialize dummy data
        userBasicInfo.create();
    }
}
