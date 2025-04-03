package org.example.springdemo.config;

import org.example.springdemo.repository.AppRepository;
import org.example.springdemo.service.SecurityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AppRepository userRepo;

    //system.out.println is used for logging//
    public SecurityConfig(AppRepository userRepo) {
        this.userRepo = userRepo;
        System.out.println("SecurityConfig initialized");
    }

    @Bean
    public UserDetailsService userDetailsService() {
        SecurityService service = new SecurityService(userRepo);
        System.out.println("UserDetailsService created: " + service.getClass().getName());
        return service;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("PasswordEncoder initialized: " + encoder.getClass().getName());
        return encoder;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        System.out.println("DaoAuthenticationProvider configured");
        return provider;
    }

    @Bean
    public AuthenticationSuccessHandler successRedirectHandler() {
        return (request, response, authentication) -> {
            if (authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
                response.sendRedirect("/admin/dashboard"); // Admin goes to admin dashboard
            } else {
                response.sendRedirect("/profile"); // User goes to profile
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManagerBuilder auth) throws Exception {
        // configure the AuthenticationManager
        auth.authenticationProvider(authenticationProvider(userDetailsService(), passwordEncoder()));
        // Matcher for endpoints that should only be accessible to anonymous users
        AntPathRequestMatcher loginAndRegisterMatcher = new AntPathRequestMatcher("/login|/register");
        NegatedRequestMatcher authenticatedMatcher = new NegatedRequestMatcher(loginAndRegisterMatcher);

        http
                .authorizeHttpRequests(authorize -> authorize
                        // Allow anonymous access to public endpoints
                        .requestMatchers("/", "/regsuccess","/network/search","/networkprofile","/networkprofile/**",
                                "/forgetpass", "/resetpass/**","/network", "/css/**", "/js/**", "/images/**","/fragments/**").permitAll()
                        // Restrict /login and /register to anonymous users only
                        .requestMatchers("/login", "/register").anonymous()
                        // Require authentication for profile and other endpoints
                        .requestMatchers("/profile").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Only admins access /admin/**
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(successRedirectHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/login?logout")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        // Redirect authenticated users trying to access /login or /register
                        .defaultAuthenticationEntryPointFor(
                                new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/profile"),
                                loginAndRegisterMatcher
                        )
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/admin/dashboard","/upload/profileimage")  // Disable CSRF for POST to work
                );
        System.out.println("SecurityFilterChain configured");
        return http.build();
    }
}