package com.carry_guide.carry_guide_admin.model.entity;

import com.carry_guide.carry_guide_admin.dto.enums.AccountStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "customer_info",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        })
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "username")
    private String userName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "address")
    private String address;

    @Size(max =50)
    @Column(name = "mobile_number")
    private String mobileNumber;

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
    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", length = 20)
    private AccountStatus userAccountStatus;


    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}
