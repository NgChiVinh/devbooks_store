package com.devbooks.config;

import com.devbooks.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.devbooks.config.CustomLoginSuccessHandler; // Đảm bảo import này đúng

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Tiêm (Inject) Handler gộp giỏ hàng
    @Autowired
    private CustomLoginSuccessHandler loginSuccessHandler;

    @Autowired
    private UserService userService;

    // Tiêm (Inject) Encoder (được tạo từ CloudinaryConfig)
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder); // Dùng encoder đã tiêm
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. Tĩnh
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // 2. CÔNG KHAI
                        .requestMatchers(
                                "/", "/home", "/register", "/book/**", "/login",
                                "/products", "/category/**", "/search", "/about", "/contact"
                        ).permitAll()

                        // 3. ✅ SỬA LỖI GIỎ HÀNG
                        // Cho phép tất cả các đường dẫn con (như /cart/add/...)
                        .requestMatchers("/cart/**").permitAll()

                        // 4. Admin
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 5. Khác (như /checkout)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(loginSuccessHandler) // Dùng Handler gộp giỏ
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .authenticationProvider(authenticationProvider());

        return http.build();
    }
}