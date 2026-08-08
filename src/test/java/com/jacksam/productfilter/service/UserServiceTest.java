package com.jacksam.productfilter.service;

import com.jacksam.productfilter.dto.UserDTO;
import com.jacksam.productfilter.entity.Role;
import com.jacksam.productfilter.entity.User;
import com.jacksam.productfilter.repository.RoleRepository;
import com.jacksam.productfilter.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditService auditService;

    @InjectMocks private UserService userService;

    @Test
    void createUser_success_encodesPasswordAndAssignsRole() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed-secret");
        when(roleRepository.findByName("VIEWER")).thenReturn(
                Optional.of(new Role("VIEWER", "Read-only")));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });

        UserDTO dto = userService.createUser(
                "alice", "secret", "alice@x.com", "Alice", "VIEWER", 1L);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.username()).isEqualTo("alice");
        assertThat(dto.roles()).containsExactly("VIEWER");
        assertThat(dto.departmentId()).isEqualTo(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_duplicateUsername_throws() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(
                "alice", "x", "a@x.com", "Alice", null, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists: alice");
    }

    @Test
    void createUser_unknownRole_throws() {
        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(roleRepository.findByName("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(
                "bob", "x", "b@x.com", "Bob", "GHOST", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role not found: GHOST");
    }

    @Test
    void createUser_noRole_createsWithoutRoles() {
        when(userRepository.existsByUsername("carol")).thenReturn(false);
        when(passwordEncoder.encode("x")).thenReturn("h");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDTO dto = userService.createUser(
                "carol", "x", "c@x.com", "Carol", null, null);

        assertThat(dto.roles()).isEmpty();
    }

    @Test
    void getUser_found_returnsDto() {
        User user = new User("alice", "pw", "a@x.com", "Alice");
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO dto = userService.getUser(1L);

        assertThat(dto.username()).isEqualTo("alice");
    }

    @Test
    void getUser_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found: 99");
    }
}
