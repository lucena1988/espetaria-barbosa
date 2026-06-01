package br.com.espetariabarbosa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FuncionarioUserDetailsService implements UserDetailsService {

    private final FuncionarioService funcionarioService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            var funcionario = funcionarioService.buscarPorUsernameAtivo(username);
            return User.builder()
                    .username(funcionario.getUsername())
                    .password(funcionario.getSenha())
                    .roles(funcionario.getPerfil().name())
                    .build();
        } catch (RuntimeException exception) {
            throw new UsernameNotFoundException("Usuario nao encontrado", exception);
        }
    }
}
