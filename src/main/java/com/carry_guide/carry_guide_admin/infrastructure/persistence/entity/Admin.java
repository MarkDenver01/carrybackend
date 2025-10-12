package com.carry_guide.carry_guide_admin.infrastructure.persistence.entity;

import com.carry_guide.carry_guide_admin.domain.enums.AccountStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@Table(name = "admin_info",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        })
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "username")
    private String userName;

    @NotBlank
    @Size(max =50)
    @Email
    @Column(name = "email")
    private String email;

    @Column(name = "created_date")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "MMM dd, yyyy hh:mm a",
            timezone = "Asia/Manila" // or GMT+8
    )
    private LocalDate createdDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", length = 20)
    private AccountStatus accountStatus;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}
