package loja.dominio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pedido de compra. Concentra as regras de valor e de transição de status. */
public class Pedido {

    private final int id;
    private final Cliente cliente;
    private final List<ItemPedido> itens;

    private String modalidadeFrete;
    private BigDecimal valorFrete = BigDecimal.ZERO;
    private int prazoEntregaDias;

    private StatusPedido status = StatusPedido.AGUARDANDO_PAGAMENTO;
    private String formaPagamento;
    private String codigoTransacao;

    public Pedido(int id, Cliente cliente, List<ItemPedido> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Pedido precisa ter pelo menos um item");
        }
        this.id = id;
        this.cliente = cliente;
        this.itens = new ArrayList<>(itens);
    }

    public BigDecimal getSubtotal() {
        BigDecimal soma = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            soma = soma.add(item.getSubtotal());
        }
        return soma;
    }

    public double getPesoTotalKg() {
        double peso = 0;
        for (ItemPedido item : itens) {
            peso += item.getPesoKg();
        }
        return peso;
    }

    public BigDecimal getTotal() {
        return getSubtotal().add(valorFrete);
    }

    public void aplicarFrete(String modalidade, BigDecimal valor, int prazoDias) {
        this.modalidadeFrete = modalidade;
        this.valorFrete = valor;
        this.prazoEntregaDias = prazoDias;
    }

    public void registrarPagamento(String formaPagamento, String codigoTransacao) {
        this.formaPagamento = formaPagamento;
        this.codigoTransacao = codigoTransacao;
    }

    /** Muda o status validando a transição. Lança exceção se a transição for inválida. */
    public void alterarStatus(StatusPedido novo) {
        if (!status.podeMudarPara(novo)) {
            throw new IllegalStateException(
                    "Pedido #" + id + ": transicao invalida de " + status + " para " + novo);
        }
        this.status = novo;
    }

    public int getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public List<ItemPedido> getItens() { return Collections.unmodifiableList(itens); }
    public String getModalidadeFrete() { return modalidadeFrete; }
    public BigDecimal getValorFrete() { return valorFrete; }
    public int getPrazoEntregaDias() { return prazoEntregaDias; }
    public StatusPedido getStatus() { return status; }
    public String getFormaPagamento() { return formaPagamento; }
    public String getCodigoTransacao() { return codigoTransacao; }
}
