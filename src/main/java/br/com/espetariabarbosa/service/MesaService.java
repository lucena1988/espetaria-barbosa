package br.com.espetariabarbosa.service;

import br.com.espetariabarbosa.entity.Mesa;
import br.com.espetariabarbosa.enums.StatusMesa;
import br.com.espetariabarbosa.repository.MesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MesaService {

    private final MesaRepository mesaRepository;

    public List<Mesa> listarAtivas() {
        return mesaRepository.findByAtivaTrueOrderByNumeroAsc();
    }

    public List<Mesa> listarDisponiveisParaPedido() {
        return mesaRepository.findByStatusInAndAtivaTrueOrderByNumeroAsc(
                List.of(StatusMesa.LIVRE, StatusMesa.OCUPADA)
        );
    }

    public Mesa salvar(Mesa mesa) {
        return mesaRepository.save(mesa);
    }

    public Mesa buscarPorId(Long id) {
        return mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa nao encontrada"));
    }

    public void alterarStatus(Long id, StatusMesa status) {
        Mesa mesa = buscarPorId(id);
        mesa.setStatus(status);
        mesaRepository.save(mesa);
    }

    public void atualizarStatusPorNumeroSeExistir(String numero, StatusMesa status) {
        if (numero == null || numero.isBlank()) {
            return;
        }

        mesaRepository.findByNumeroAndAtivaTrue(numero)
                .ifPresent(mesa -> {
                    mesa.setStatus(status);
                    mesaRepository.save(mesa);
                });
    }
}
