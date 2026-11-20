package loja.repositorio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import loja.dominio.Produto;

/**
 * Catálogo de produtos em memória.
 * Decisão consciente: sem interface e sem Singleton (ver análise crítica).
 */
public class RepositorioProdutos {

    private final Map<Integer, Produto> produtos = new LinkedHashMap<>();

    public void salvar(Produto produto) {
        produtos.put(produto.getId(), produto);
    }

    public Optional<Produto> buscarPorId(int id) {
        return Optional.ofNullable(produtos.get(id));
    }

    public List<Produto> listarTodos() {
        return new ArrayList<>(produtos.values());
    }

    /** Catálogo inicial usado na demonstração. */
    public static RepositorioProdutos comCatalogoInicial() {
        RepositorioProdutos repo = new RepositorioProdutos();
        repo.salvar(new Produto(1, "Teclado mecanico", new BigDecimal("249.90"), 1.2, 10));
        repo.salvar(new Produto(2, "Mouse sem fio", new BigDecimal("89.90"), 0.3, 25));
        repo.salvar(new Produto(3, "Monitor 24 pol", new BigDecimal("899.00"), 4.5, 5));
        repo.salvar(new Produto(4, "Cabo HDMI 2m", new BigDecimal("29.90"), 0.2, 50));
        repo.salvar(new Produto(5, "Headset gamer", new BigDecimal("199.90"), 0.8, 2));
        return repo;
    }
}
