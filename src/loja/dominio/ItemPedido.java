package loja.dominio;

import java.math.BigDecimal;

/** Uma linha do pedido: produto + quantidade. Guarda o preço do momento da compra. */
public class ItemPedido {

    private final Produto produto;
    private final int quantidade;
    private final BigDecimal precoUnitario;

    public ItemPedido(Produto produto, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = produto.getPreco();
    }

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public double getPesoKg() {
        return produto.getPesoKg() * quantidade;
    }

    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
}
