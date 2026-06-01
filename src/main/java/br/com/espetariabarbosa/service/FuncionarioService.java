package br.com.espetariabarbosa.service;

import br.com.espetariabarbosa.entity.Funcionario;
import br.com.espetariabarbosa.enums.PerfilFuncionario;
import br.com.espetariabarbosa.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Funcionario> listarTodos() {
        return funcionarioRepository.findAllByOrderByNomeAsc();
    }

    public Funcionario salvar(Funcionario funcionario) {
        if (funcionarioRepository.existsByUsername(funcionario.getUsername())) {
            throw new IllegalArgumentException("Ja existe funcionario com este usuario");
        }
        if (funcionario.getAtivo() == null) {
            funcionario.setAtivo(true);
        }
        if (funcionario.getPerfil() == null) {
            funcionario.setPerfil(PerfilFuncionario.FUNCIONARIO);
        }
        funcionario.setSenha(passwordEncoder.encode(funcionario.getSenha()));
        return funcionarioRepository.save(funcionario);
    }

    public Funcionario buscarPorUsernameAtivo(String username) {
        return funcionarioRepository.findByUsernameAndAtivoTrue(username)
                .orElseThrow(() -> new RuntimeException("Funcionario nao encontrado"));
    }
}
