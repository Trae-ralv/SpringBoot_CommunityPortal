package org.example.springdemo.service;

import org.example.springdemo.model.UserModel;
import org.example.springdemo.repository.AppRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppRepository userRepo;

    public CustomUserDetailsService(AppRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Attempting to load user with email: " + email);
        UserModel user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        System.out.println("Found user: " + user.getEmail() + ", stored password: " + user.getPassword());

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}