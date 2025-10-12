package com.carry_guide.carry_guide_admin.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialAuthenticationHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    @Autowired
    private final UserService userService;

    @Autowired
    private final JwtUtils jwtUtils;

    @Value("${base.url.backend}")
    private String baseUrl;

    String email;
    String idAttributeKey;
    @Autowired
    private RoleRepository roleRepository;

}
