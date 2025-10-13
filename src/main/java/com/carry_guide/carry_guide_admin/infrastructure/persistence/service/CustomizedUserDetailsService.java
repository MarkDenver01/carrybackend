package com.carry_guide.carry_guide_admin.infrastructure.persistence.service;

import com.carry_guide.carry_guide_admin.domain.repository.JpaUserRepository;
import com.carry_guide.carry_guide_admin.infrastructure.persistence.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomizedUserDetailsService implements UserDetailsService {

    @Autowired
    JpaUserRepository jpaUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = jpaUserRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return CustomizedUserDetails.build(user);
    }
}
