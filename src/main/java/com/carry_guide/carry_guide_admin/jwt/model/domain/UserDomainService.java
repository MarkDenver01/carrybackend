package com.carry_guide.carry_guide_admin.jwt.model.domain;

import com.carry_guide.carry_guide_admin.jwt.model.entity.Role;
import com.carry_guide.carry_guide_admin.jwt.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserDomainService {
    List<User> getAllUsers();

    List<Role> getAllRoles();

    void updateUserRoles(Long userId, String roleState);

    void updatePassword(Long userId, String newPassword);

    void generatePasswordResetToken(String email);

    void resetPassword(String token, String newPassword);

    Optional<User> findByEmail(String email);

    void registerUser(User user);
}
