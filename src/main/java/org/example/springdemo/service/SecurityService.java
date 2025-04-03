package org.example.springdemo.service;

import org.example.springdemo.model.UserModel;
import org.example.springdemo.repository.AppRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SecurityService implements UserDetailsService {

    private final AppRepository userRepo;

    public SecurityService(AppRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String Email) throws UsernameNotFoundException {
        System.out.println("Attempting to load user with email: " + Email);
        UserModel user = userRepo.findByEmail(Email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with Email: " + Email));
        Integer UserType = user.getUser_type();
        System.out.println("Found user: " + user.getEmail() + ", stored password: "
                + user.getPassword()+ user.getPassword() + ", UserType: " +UserType);

        String role ; // Default role
        if (UserType != null && UserType == 1) {
            role = "ADMIN"; // Set role to ADMIN if user_type is 1
        }else{
            role = "USER";
        }

        return User.builder()
                .username(String.valueOf(user.getUser_id())) // Use user_id as principal
                .password(user.getPassword())
                .roles(role) // Assign dynamic role based on user_type
                .build();
    }
}

