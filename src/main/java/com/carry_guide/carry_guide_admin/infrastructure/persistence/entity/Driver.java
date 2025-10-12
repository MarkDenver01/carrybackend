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
import org.springframework.format.annotation.NumberFormat;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "driver_info",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        })
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_id")
    private Long driverId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "username")
    private String userName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "address")
    private String address;

    @NotBlank
    @Size(max =50)
    @NumberFormat(style = NumberFormat.Style.NUMBER)
    @Column(name = "mobile_number")
    private int mobileNumber;

    @NotBlank
    @Size(max =50)
    @Email
    @Column(name = "email")
    private String email;

    @NotBlank
    @Size(max =50)
    @Column(name = "drivers_license_number")
    private String driversLicenseNumber;

    @Column(name = "front_id_url")
    private String frontIdUrl;

    @Column(name = "back_id_url")
    private String backIdUrl;

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
