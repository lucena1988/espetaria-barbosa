package br.com.espetariabarbosa.repository;

import br.com.espetariabarbosa.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByAtivoTrueOrderByNomeAsc();
    List<Cliente> findByAtivoFalseOrderByNomeAsc();
    List<Cliente> findAllByOrderByNomeAsc();
}
