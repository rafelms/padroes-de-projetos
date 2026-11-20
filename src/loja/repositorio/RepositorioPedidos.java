package loja.repositorio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import loja.dominio.Pedido;

/** Armazena pedidos em memória e gera os IDs sequenciais. */
public class RepositorioPedidos {

    private final Map<Integer, Pedido> pedidos = new LinkedHashMap<>();
    private int ultimoId = 0;

    public int proximoId() {
        return ++ultimoId;
    }

    public void salvar(Pedido pedido) {
        pedidos.put(pedido.getId(), pedido);
    }

    public Optional<Pedido> buscarPorId(int id) {
        return Optional.ofNullable(pedidos.get(id));
    }

    public List<Pedido> listarTodos() {
        return new ArrayList<>(pedidos.values());
    }
}
