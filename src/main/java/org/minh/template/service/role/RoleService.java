package org.minh.template.service.role;

import org.minh.template.entity.Role;
import org.minh.template.util.Constants;

public interface RoleService {
    Role findByName(final Constants.RoleEnum name);
    Role create(final Role role);
}
