package br.com.espetariabarbosa.repository;

import br.com.espetariabarbosa.entity.Funcionario;
import br.com.espetariabarbosa.enums.PerfilFuncionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    Optional<Funcionario> findByUsernameAndAtivoTrue(String username);
    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, Long id);
    long countByPerfilAndAtivoTrue(PerfilFuncionario perfil);
    List<Funcionario> findAllByOrderByAtivoDescNomeAsc();
}
