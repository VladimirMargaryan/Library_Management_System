package com.app.library.service;

import com.app.library.exception.NotFoundException;
import com.app.library.model.Role;

import java.util.List;

public interface RoleService {

    Role getByID(Long id) throws NotFoundException;
    Role getByName(String roleName);
    Role save(Role role);
    List<Role> getAll();
    void removeById(Long id) throws NotFoundException;
    Role update(Role role) throws NotFoundException;

}
