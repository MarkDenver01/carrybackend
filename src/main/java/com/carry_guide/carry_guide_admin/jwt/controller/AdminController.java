package com.carry_guide.carry_guide_admin.jwt.controller;

import com.carry_guide.carry_guide_admin.jwt.model.entity.Role;
import com.carry_guide.carry_guide_admin.jwt.model.entity.User;
import com.carry_guide.carry_guide_admin.jwt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    UserService userService;

    @GetMapping("/get_users")
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @PutMapping("/update_user_role")
    public ResponseEntity<String> updateUserRole(@RequestParam Long userId, @RequestParam String role) {
        userService.updateUserRoles(userId, role);
        return new ResponseEntity<>("Successfully updated user role", HttpStatus.OK);
    }

    @GetMapping("/get_user_roles")
    public List<Role> getUserRoles() {
        return userService.getAllRoles();
    }

    @PutMapping("/update_user_password")
    public ResponseEntity<String> updatePassword(@RequestParam Long userId, String password) {
        try {
            userService.updatePassword(userId, password);
            return new ResponseEntity<>("Successfully updated password", HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
