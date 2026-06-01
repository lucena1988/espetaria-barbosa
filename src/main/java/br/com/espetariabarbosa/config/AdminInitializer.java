package br.com.espetariabarbosa.config;

import br.com.espetariabarbosa.entity.Funcionario;
import br.com.espetariabarbosa.enums.PerfilFuncionario;
import br.com.espetariabarbosa.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (funcionarioRepository.existsByUsername("admin")) {
            return;
        }

        Funcionario admin = Funcionario.builder()
                .nome("Administrador")
                .username("admin")
                .senha(passwordEncoder.encode("admin123"))
                .perfil(PerfilFuncionario.ADMIN)
                .ativo(true)
                .build();

        funcionarioRepository.save(admin);
    }
}
