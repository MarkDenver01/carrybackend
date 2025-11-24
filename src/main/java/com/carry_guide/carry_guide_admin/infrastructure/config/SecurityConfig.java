package com.carry_guide.carry_guide_admin.infrastructure.config;

import com.carry_guide.carry_guide_admin.domain.enums.AccountStatus;
import com.carry_guide.carry_guide_admin.domain.enums.RoleState;
import com.carry_guide.carry_guide_admin.model.entity.Admin;
import com.carry_guide.carry_guide_admin.model.entity.Role;
import com.carry_guide.carry_guide_admin.model.entity.User;
import com.carry_guide.carry_guide_admin.repository.JpaAdminRepository;
import com.carry_guide.carry_guide_admin.repository.JpaRoleRepository;
import com.carry_guide.carry_guide_admin.repository.JpaUserRepository;
import com.carry_guide.carry_guide_admin.infrastructure.security.AuthEntryPoint;
import com.carry_guide.carry_guide_admin.infrastructure.security.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Autowired
    private AuthEntryPoint unAuthorizedHandler;

    @Autowired
    @Lazy
    private SocialAuthenticationHandler authorizedHandler;

    @Autowired
    private CorsConfig corsConfig;

    /**
     * Filter to handle JWT token in request header.
     */
    @Bean
    public AuthTokenFilter authTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * Password encoder used for hashing passwords (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring authentication manager bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Initialize default roles and users (super admin, admin) if not present in DB.
     */
    @Bean
    public CommandLineRunner init(
            JpaRoleRepository roleRepository,
            JpaUserRepository userRepository,
            JpaAdminRepository adminRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Role adminRole = roleRepository.findRoleByRoleState(RoleState.ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(RoleState.ADMIN)));

            Role subAdminRole = roleRepository.findRoleByRoleState(RoleState.SUB_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(RoleState.SUB_ADMIN)));

            if (!userRepository.existsByEmail("wrapandcarry@admin.com")) {
                User user = new User();
                user.setUserName("Administrator");
                user.setEmail("wrapandcarry@admin.com");
                user.setPassword(passwordEncoder.encode("admin"));
                user.setSignupMethod("email");
                user.setRole(adminRole);
                user.setMobileNumber("09621531667");
                userRepository.save(user);

                Admin admin = new Admin();
                admin.setUserName(user.getUserName());
                admin.setEmail(user.getEmail());
                admin.setAccountStatus(AccountStatus.VERIFIED);

                // LocalDate -> LocalDateTime change handled here
                admin.setCreatedDate(LocalDateTime.now());

                // profileUrl column exists now; set to null or a default if desired
                admin.setProfileUrl(null);

                admin.setUser(user);

                adminRepository.save(admin);
            }

            if (!userRepository.existsByEmail("wrapandcarry@subadmin.com")) {
                User newUser = new User();
                newUser.setUserName("Sub Admin");
                newUser.setEmail("wrapandcarry@subadmin.com");
                newUser.setPassword(passwordEncoder.encode("admin"));
                newUser.setSignupMethod("email");
                newUser.setRole(subAdminRole);
                newUser.setMobileNumber("09621531668");
                userRepository.save(newUser);

                Admin subAdmin = new Admin();
                subAdmin.setUserName(newUser.getUserName());
                subAdmin.setEmail(newUser.getEmail());
                subAdmin.setAccountStatus(AccountStatus.VERIFIED);

                // LocalDate -> LocalDateTime change handled here
                subAdmin.setCreatedDate(LocalDateTime.now());

                // profileUrl column exists now; set to null or a default if desired
                subAdmin.setProfileUrl(null);

                subAdmin.setUser(newUser);

                adminRepository.save(subAdmin);
            }
        };
    }

    /**
     * Main Spring Security filter chain configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless JWT APIs (if you have endpoints that need CSRF, re-enable accordingly)
                .csrf(csrf -> csrf.disable())

                // Enable CORS with custom configuration
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/csrf_token").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/api/gmail/**").permitAll()
                        .requestMatchers("/user/public/**").permitAll()
                        .requestMatchers("/upload/**").permitAll()
                        .requestMatchers("/upload/driver/**").permitAll()
                        .requestMatchers("/upload/product/**").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/admin/**").hasAnyAuthority("ADMIN", "SUB_ADMIN")
                        .requestMatchers("/customer/**").hasAuthority("CUSTOMER")
                        .requestMatchers("/driver/**").hasAuthority("RIDER")
                        .anyRequest().authenticated())

                // OAuth2 login handling (success handler provided)
                .oauth2Login(oauth -> oauth.successHandler(authorizedHandler))

                // Exception handling (unauthorized access)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unAuthorizedHandler))

                // Add JWT token filter before the username/password filter
                .addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class)

                // Make session stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Enable form login and basic auth (optional)
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
