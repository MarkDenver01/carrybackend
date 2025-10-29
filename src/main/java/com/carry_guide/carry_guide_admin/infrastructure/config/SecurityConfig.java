package com.carry_guide.carry_guide_admin.infrastructure.config;

import com.carry_guide.carry_guide_admin.dto.enums.RoleState;
import com.carry_guide.carry_guide_admin.model.entity.Role;
import com.carry_guide.carry_guide_admin.model.entity.User;
import com.carry_guide.carry_guide_admin.repository.JpaRoleRepository;
import com.carry_guide.carry_guide_admin.repository.JpaUserRepository;
import com.carry_guide.carry_guide_admin.infrastructure.security.AuthEntryPoint;
import com.carry_guide.carry_guide_admin.infrastructure.security.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
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
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Role adminRole = roleRepository.findRoleByRoleState(RoleState.ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(RoleState.ADMIN)));

            if (!userRepository.existsByEmail("wrapandcarry@admin.com")) {
                User user = new User(
                        "Administrator",
                        "wrapandcarry@admin.com",
                        passwordEncoder.encode("admin"));
                user.setSignupMethod("email");
                user.setRole(adminRole);
                user.setMobileNumber("09621531667");
                userRepository.save(user);
            }
        };
    }

    /**
     * Main Spring Security filter chain configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CSRF protection except for these endpoints
//                .csrf(csrf -> csrf
//                        .ignoringRequestMatchers("/user/public/login", "/user/public/register")
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .csrf(csrf -> csrf.disable()) // disable csrf (recommended for APIs with JWT)

                // Enable CORS with custom configuration
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/csrf_token").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/user/public/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated())

                // OAuth2 login handling
                .oauth2Login(oauth -> oauth.successHandler(authorizedHandler))

                // Exception handling (unauthorized access)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unAuthorizedHandler))

                // Add JWT token filter before the username/password filter
                .addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class)

                // configures Spring Security to not use HTTP sessions at all.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Enable form login and basic auth
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
