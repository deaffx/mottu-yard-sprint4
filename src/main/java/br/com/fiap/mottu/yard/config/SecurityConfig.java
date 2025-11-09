package br.com.fiap.mottu.yard.config;

import br.com.fiap.mottu.yard.security.CustomLogout;
import br.com.fiap.mottu.yard.security.OAuth2Login;
import br.com.fiap.mottu.yard.security.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2Service oauth2Service;
    private final OAuth2Login oauth2Login;
    private final CustomLogout customLogout;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/", "/error", "/oauth2/**", "/webjars/**",
                                "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/auth/selecionar-perfil", "/auth/selecionar-perfil/**").authenticated()
                        .requestMatchers("/dashboard", "/home", "/home/**").hasAnyRole("OPERADOR", "MECANICO")
                        .requestMatchers("/manutencao/**").hasAnyRole("OPERADOR", "MECANICO")
                        .requestMatchers("/motos/**", "/patios/**").hasRole("OPERADOR")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2Service))
                        .successHandler(oauth2Login)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(customLogout)
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .permitAll()
                );

        return http.build();
    }
}