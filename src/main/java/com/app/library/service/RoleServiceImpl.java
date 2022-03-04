package com.app.library.service;

import com.app.library.exception.NotFoundException;
import com.app.library.model.Role;
import com.app.library.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService{

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role getByID(Long id) throws NotFoundException {
        Optional<Role> optionalRole = roleRepository.findById(id);
        if (!optionalRole.isPresent()) {
            log.info("Role not found by the id " + id);
            throw new NotFoundException("Role not found!");
        }
        log.info("Role founded! " + optionalRole.get());
        return optionalRole.get();
    }

    @Override
    public Role getByName(String roleName) {
        Role founded = roleRepository.findByName(roleName);
        if (founded == null)
            log.info("Role not founded by the name " + roleName);
        return founded;
    }


    @Transactional
    @Override
    public Role save(Role role) {
        Role saved = roleRepository.save(role);
        log.info("Role saved! " + saved);
        return saved;
    }

    @Override
    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    @Transactional
    @Override
    public void removeById(Long id) {
        roleRepository.deleteById(id);
        log.info("Role by th id " + id + " deleted!");
    }

    @Transactional
    @Override
    public Role update(Role role) throws NotFoundException {
        Role newRole = getByID(role.getId());
        newRole.setName(role.getName());
        Role updated = save(newRole);
        log.info("Role updated! " + updated);
        return updated;
    }
}
