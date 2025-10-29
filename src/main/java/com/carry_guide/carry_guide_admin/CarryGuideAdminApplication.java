package com.carry_guide.carry_guide_admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.carry_guide.carry_guide_admin.repository")
@EntityScan(basePackages = "com.carry_guide.carry_guide_admin.model.entity")
public class CarryGuideAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarryGuideAdminApplication.class, args);
	}

}
