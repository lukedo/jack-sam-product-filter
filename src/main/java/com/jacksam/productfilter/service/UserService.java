package com.jacksam.productfilter.service;

import com.jacksam.productfilter.dto.UserDTO;
import com.jacksam.productfilter.entity.Role;
import com.jacksam.productfilter.entity.User;
import com.jacksam.productfilter.enums.AuditAction;
import com.jacksam.productfilter.repository.RoleRepository;
import com.jacksam.productfilter.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public UserDTO createUser(String username, String password, String email, String displayName,
                              String roleName, Long departmentId) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }

        User user = new User(username, passwordEncoder.encode(password), email, displayName);
        user.setDepartmentId(departmentId);

        if (roleName != null) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            user.setRoles(Set.of(role));
        }

        user = userRepository.save(user);
        return UserDTO.from(user);
    }

    public UserDTO getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return UserDTO.from(user);
    }
}
