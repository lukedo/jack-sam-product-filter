package com.jacksam.productfilter.dto;

import com.jacksam.productfilter.entity.User;
import java.util.List;

public record UserDTO(
        Long id,
        String username,
        String email,
        String displayName,
        List<String> roles,
        Long departmentId
) {
    public static UserDTO from(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRoles().stream().map(r -> r.getName()).toList(),
                user.getDepartmentId()
        );
    }
}
