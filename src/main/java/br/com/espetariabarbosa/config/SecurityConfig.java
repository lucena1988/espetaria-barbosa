package br.com.espetariabarbosa.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/login", "/cardapio/**").permitAll()
                        .requestMatchers("/funcionarios", "/funcionarios/**").hasRole("ADMIN")
                        .requestMatchers("/produtos", "/produtos/**", "/categorias", "/categorias/**").hasRole("ADMIN")
                        .requestMatchers("/dashboard", "/dashboard/**", "/pagamentos", "/pagamentos/**").hasAnyRole("ADMIN", "CAIXA", "FUNCIONARIO")
                        .requestMatchers("/clientes", "/clientes/**").hasAnyRole("ADMIN", "CAIXA", "ATENDENTE", "FUNCIONARIO")
                        .requestMatchers("/mesas", "/mesas/**").hasAnyRole("ADMIN", "CAIXA", "ATENDENTE", "FUNCIONARIO")
                        .requestMatchers("/pedidos", "/pedidos/**").hasAnyRole("ADMIN", "CAIXA", "ATENDENTE", "FUNCIONARIO")
                        .requestMatchers("/cozinha", "/cozinha/**", "/painel").hasAnyRole("ADMIN", "COZINHA", "FUNCIONARIO")
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            var authorities = authentication.getAuthorities().stream()
                                    .map(Object::toString)
                                    .toList();

                            if (authorities.contains("ROLE_COZINHA")) {
                                response.sendRedirect("/cozinha");
                                return;
                            }

                            if (authorities.contains("ROLE_ATENDENTE")) {
                                response.sendRedirect("/pedidos");
                                return;
                            }

                            response.sendRedirect("/dashboard");
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
