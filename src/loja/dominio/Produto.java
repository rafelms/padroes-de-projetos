package loja.dominio;

import java.math.BigDecimal;

/** Produto vendido pela loja. Controla o próprio estoque. */
public class Produto {

    private final int id;
    private final String nome;
    private final BigDecimal preco;
    private final double pesoKg;
    private int estoque;

    public Produto(int id, String nome, BigDecimal preco, double pesoKg, int estoque) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.pesoKg = pesoKg;
        this.estoque = estoque;
    }

    public void baixarEstoque(int quantidade) {
        if (quantidade > estoque) {
            throw new IllegalStateException("Estoque insuficiente para " + nome);
        }
        estoque -= quantidade;
    }

    public void devolverEstoque(int quantidade) {
        estoque += quantidade;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public BigDecimal getPreco() { return preco; }
    public double getPesoKg() { return pesoKg; }
    public int getEstoque() { return estoque; }
}
