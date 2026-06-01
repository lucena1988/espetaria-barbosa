package br.com.espetariabarbosa.repository;

import br.com.espetariabarbosa.entity.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    Optional<Funcionario> findByUsernameAndAtivoTrue(String username);
    boolean existsByUsername(String username);
    List<Funcionario> findAllByOrderByNomeAsc();
}
