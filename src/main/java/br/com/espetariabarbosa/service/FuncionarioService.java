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
        return funcionarioRepository.findAllByOrderByAtivoDescNomeAsc();
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

    public Funcionario editar(Long id, Funcionario dadosAtualizados) {
        Funcionario funcionario = buscarPorId(id);

        if (funcionarioRepository.existsByUsernameAndIdNot(dadosAtualizados.getUsername(), id)) {
            throw new IllegalArgumentException("Ja existe funcionario com este usuario");
        }

        validarUltimoAdministrador(funcionario, dadosAtualizados.getPerfil(), funcionario.getAtivo());

        funcionario.setNome(dadosAtualizados.getNome());
        funcionario.setUsername(dadosAtualizados.getUsername());
        funcionario.setPerfil(dadosAtualizados.getPerfil() == null ? PerfilFuncionario.FUNCIONARIO : dadosAtualizados.getPerfil());

        if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().isBlank()) {
            funcionario.setSenha(passwordEncoder.encode(dadosAtualizados.getSenha()));
        }

        return funcionarioRepository.save(funcionario);
    }

    public Funcionario alternarStatus(Long id) {
        Funcionario funcionario = buscarPorId(id);
        Boolean novoStatus = !Boolean.TRUE.equals(funcionario.getAtivo());

        validarUltimoAdministrador(funcionario, funcionario.getPerfil(), novoStatus);

        funcionario.setAtivo(novoStatus);
        return funcionarioRepository.save(funcionario);
    }

    public Funcionario buscarPorUsernameAtivo(String username) {
        return funcionarioRepository.findByUsernameAndAtivoTrue(username)
                .orElseThrow(() -> new RuntimeException("Funcionario nao encontrado"));
    }

    private Funcionario buscarPorId(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Funcionario nao encontrado"));
    }

    private void validarUltimoAdministrador(Funcionario funcionarioAtual, PerfilFuncionario novoPerfil, Boolean novoStatus) {
        boolean eraAdminAtivo = funcionarioAtual.getPerfil() == PerfilFuncionario.ADMIN
                && Boolean.TRUE.equals(funcionarioAtual.getAtivo());
        boolean continuaraAdminAtivo = novoPerfil == PerfilFuncionario.ADMIN
                && Boolean.TRUE.equals(novoStatus);

        if (eraAdminAtivo && !continuaraAdminAtivo
                && funcionarioRepository.countByPerfilAndAtivoTrue(PerfilFuncionario.ADMIN) <= 1) {
            throw new IllegalArgumentException("Nao e possivel remover o ultimo administrador ativo");
        }
    }
}
