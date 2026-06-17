package br.com.espetariabarbosa.service;

import br.com.espetariabarbosa.entity.Cliente;
import br.com.espetariabarbosa.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<Cliente> listarAtivos() {
        return clienteRepository.findByAtivoTrueOrderByNomeAsc();
    }

    public List<Cliente> listarInativos() {
        return clienteRepository.findByAtivoFalseOrderByNomeAsc();
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAllByOrderByNomeAsc();
    }

    public Cliente salvar(Cliente cliente) {
        if (cliente.getAtivo() == null) {
            cliente.setAtivo(true);
        }
        return clienteRepository.save(cliente);
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente nao encontrado"));
    }

    public void arquivar(Long id) {
        Cliente cliente = buscarPorId(id);
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    public void reativar(Long id) {
        Cliente cliente = buscarPorId(id);
        cliente.setAtivo(true);
        salvar(cliente);
    }
}
