package br.com.espetariabarbosa.repository;

import br.com.espetariabarbosa.entity.Mesa;
import br.com.espetariabarbosa.enums.StatusMesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MesaRepository extends JpaRepository<Mesa, Long> {
    List<Mesa> findByAtivaTrueOrderByNumeroAsc();
    List<Mesa> findByStatusInAndAtivaTrueOrderByNumeroAsc(List<StatusMesa> status);
    Optional<Mesa> findByNumeroAndAtivaTrue(String numero);
}
