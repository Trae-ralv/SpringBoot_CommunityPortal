package org.example.springdemo.config;

import org.example.springdemo.repository.AppRepository;
import org.example.springdemo.service.CustomUserDetailsService;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AppRepository userRepo;

    public SecurityConfig(AppRepository userRepo) {
        this.userRepo = userRepo;
        System.out.println("SecurityConfig initialized");
    }

    @Bean
    public UserDetailsService userDetailsService() {
        CustomUserDetailsService service = new CustomUserDetailsService(userRepo);
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManagerBuilder auth) throws Exception {
        // configure the AuthenticationManager
        auth.authenticationProvider(authenticationProvider(userDetailsService(), passwordEncoder()));
        // Matcher for endpoints that should only be accessible to anonymous users
        AntPathRequestMatcher loginAndRegisterMatcher = new AntPathRequestMatcher("/login|/register");
        NegatedRequestMatcher authenticatedMatcher = new NegatedRequestMatcher(loginAndRegisterMatcher);

        http
                .authorizeHttpRequests(authorize -> authorize
                        // Allow anonymous access to public endpoints
                        .requestMatchers("/", "/regsuccess","/network/search","/networkprofile","/networkprofile/**", "/forgetpass", "/resetpass","/network", "/css/**", "/js/**", "/images/**").permitAll()
                        // Restrict /login and /register to anonymous users only
                        .requestMatchers("/login", "/register").anonymous()
                        // Require authentication for profile and other endpoints
                        .requestMatchers("/profile").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/profile")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        // Redirect authenticated users trying to access /login or /register
                        .defaultAuthenticationEntryPointFor(
                                new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/profile"),
                                loginAndRegisterMatcher
                        )
                );
        System.out.println("SecurityFilterChain configured");
        return http.build();
    }
}