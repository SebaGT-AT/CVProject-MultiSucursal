package com.multisucursal.inventory.user.service;

import com.multisucursal.inventory.user.entity.AppUser;
import com.multisucursal.inventory.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmailIgnoreCase(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.builder()
            .username(user.getEmail())
            .password(user.getPassword())
            .authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            .disabled(!user.isEnabled())
            .build();
    }
}

