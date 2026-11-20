package loja.apresentacao;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import loja.dominio.ItemPedido;
import loja.dominio.Pedido;
import loja.dominio.Produto;

/** Funções de exibição no console (só apresentação, sem regra de negócio). */
public final class Formatador {

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    private Formatador() { }

    public static String moeda(BigDecimal valor) {
        return MOEDA.format(valor).replace(' ', ' ');
    }

    public static String produto(Produto p) {
        return String.format("  [%d] %-18s %12s  %.1fkg  estoque: %d",
                p.getId(), p.getNome(), moeda(p.getPreco()), p.getPesoKg(), p.getEstoque());
    }

    public static String pedido(Pedido p) {
        StringBuilder sb = new StringBuilder();
        sb.append("  Pedido #").append(p.getId()).append(" - ").append(p.getCliente().getNome())
          .append(" - status: ").append(p.getStatus()).append('\n');
        for (ItemPedido item : p.getItens()) {
            sb.append(String.format("     %dx %-18s %12s%n",
                    item.getQuantidade(), item.getProduto().getNome(), moeda(item.getSubtotal())));
        }
        sb.append("     Subtotal: ").append(moeda(p.getSubtotal())).append('\n');
        sb.append("     Frete (").append(p.getModalidadeFrete()).append(", ")
          .append(p.getPrazoEntregaDias()).append(" dia(s)): ").append(moeda(p.getValorFrete())).append('\n');
        sb.append("     TOTAL: ").append(moeda(p.getTotal()));
        if (p.getCodigoTransacao() != null) {
            sb.append("\n     Pago via ").append(p.getFormaPagamento())
              .append(" | transacao: ").append(p.getCodigoTransacao());
        }
        return sb.toString();
    }
}
