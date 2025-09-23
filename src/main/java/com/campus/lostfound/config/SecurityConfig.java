package com.campus.lostfound.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // 公开访问的页面
                .requestMatchers(new AntPathRequestMatcher("/demo")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/test-login")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/auth/login")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/auth/register")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/search")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/items/*")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/about")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/help")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/items/*/detail")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/css/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/js/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/images/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/uploads/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/test-users")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/")).permitAll()
                // 需要认证的页面
                .requestMatchers(new AntPathRequestMatcher("/items/post")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/items/my-items")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/items/*/edit")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/items/*/delete")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/items/*/confirm-completion")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/notifications")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/chat")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/profile")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/profile/**")).authenticated()
                // 管理员页面
                .requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN")
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessHandler(logoutSuccessHandler())
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"))
            )
            .headers(headers -> headers
                .frameOptions().sameOrigin()
            );
        
        // H2控制台配置
        http.headers().frameOptions().disable();
        
        return http.build();
    }
    
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            String targetUrl = isAdmin ? "/admin" : "/";
            response.sendRedirect(request.getContextPath() + targetUrl);
        };
    }
    
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            response.sendRedirect(request.getContextPath() + "/auth/login?logout=true");
        };
    }
}