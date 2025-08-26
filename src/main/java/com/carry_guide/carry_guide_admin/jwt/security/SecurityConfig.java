package com.carry_guide.carry_guide_admin.jwt.security;

import com.carry_guide.carry_guide_admin.jwt.config.OAuth2LoginSuccessHandler;
import com.carry_guide.carry_guide_admin.jwt.model.entity.Role;
import com.carry_guide.carry_guide_admin.jwt.model.entity.User;
import com.carry_guide.carry_guide_admin.jwt.model.state.RoleState;
import com.carry_guide.carry_guide_admin.jwt.repository.RoleRepository;
import com.carry_guide.carry_guide_admin.jwt.repository.UserRepository;
import com.carry_guide.carry_guide_admin.jwt.security.jwt.AuthEntryPoint;
import com.carry_guide.carry_guide_admin.jwt.security.jwt.AuthTokenFilter;
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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true,
jsr250Enabled = true)
public class SecurityConfig {
    @Autowired
    private AuthEntryPoint unAuthorizedHandler;

    @Autowired
    @Lazy
    OAuth2LoginSuccessHandler authorizedHandler;

    @Autowired
    CorsConfig corsConfig;

    @Bean
    public AuthTokenFilter authTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner init(RoleRepository roleRepository,
                                  UserRepository userRepository,
                                  PasswordEncoder passwordEncoder) {
        return args -> {
            Role userRole = roleRepository.findRoleByRoleState(RoleState.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(RoleState.ROLE_ADMIN)));


            if (!userRepository.existsByEmail("cares@admin.com")) {
                User user = new User(
                        "Administrator",
                        "cares@admin.com",
                        passwordEncoder.encode("admin"));
                user.setSignupMethod("email");
                user.setRole(userRole);
                userRepository.save(user);
            }
        };
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf ->
                csrf.ignoringRequestMatchers("/auth/public/user_login")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
        http.cors(cors -> corsConfig.corsConfigurationSource());
        http.authorizeHttpRequests((requests)
        -> requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/csrf_token").permitAll()
                        .requestMatchers("/auth/public/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oAuth2Login
                        -> oAuth2Login.successHandler(authorizedHandler));
        http.exceptionHandling(exception ->
                exception.authenticationEntryPoint(unAuthorizedHandler));
        http.addFilterBefore(authTokenFilter(),
                UsernamePasswordAuthenticationFilter.class);
        http.formLogin(Customizer.withDefaults());
        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
