package com.carry_guide.carry_guide_admin.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "user_account", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email"}),
        @UniqueConstraint(columnNames = {"mobile_number"})
})
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String userName;
    private String email;
    private String mobileNumber;
    private String password;
    private String signupMethod; // "EMAIL" or "MOBILE_OTP"
    private boolean verified;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToOne(mappedBy = "user") private Admin admin;
    @OneToOne(mappedBy = "user") private Customer customer;
    @OneToOne(mappedBy = "user") private Driver driver;
}
