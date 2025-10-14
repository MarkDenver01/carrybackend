package com.carry_guide.carry_guide_admin.infrastructure.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.NumberFormat;

@Entity
@Data
@NoArgsConstructor
@Table(name = "user_account",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "username")
    private String userName;

    @NotBlank
    @Size(max = 50)
    @Email
    @Column(name = "email")
    private String email;

    @NotBlank
    @Size(max = 11)
    @Column(name = "mobile_number")
    private String mobileNumber;

    @Size(max = 120)
    @Column(name = "password")
    @JsonIgnore
    private String password;

    private String signupMethod;

    private String oneTimePassword;
    private Long otpExpiry;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "role_id", referencedColumnName = "role_id")
    @JsonIgnore
    @ToString.Exclude
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Admin admin;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Customer customer;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Driver driver;

    public User(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }
}
