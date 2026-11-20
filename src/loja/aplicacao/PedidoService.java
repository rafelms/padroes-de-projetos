package loja.aplicacao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import loja.dominio.Cliente;
import loja.dominio.ItemPedido;
import loja.dominio.Pedido;
import loja.dominio.Produto;
import loja.dominio.StatusPedido;
import loja.frete.CotacaoFrete;
import loja.frete.EstrategiaFrete;
import loja.notificacao.PublicadorEventosPedido;
import loja.pagamento.DadosPagamento;
import loja.pagamento.ProcessadorPagamento;
import loja.pagamento.ProcessadorPagamentoFactory;
import loja.pagamento.ResultadoPagamento;
import loja.repositorio.RepositorioPedidos;
import loja.repositorio.RepositorioProdutos;

/**
 * Camada de APLICAÇÃO: orquestra os casos de uso do pedido.
 * É aqui que os três padrões se encontram:
 *   - Strategy  -> criarPedido() recebe a EstrategiaFrete
 *   - Factory   -> pagar() pede o processador à fábrica
 *   - Adapter   -> pagar() usa o processador sem saber qual gateway está por trás
 *   - Observer  -> mudarStatus() publica o evento para os observadores
 */
public class PedidoService {

    private final RepositorioProdutos produtos;
    private final RepositorioPedidos pedidos;
    private final ProcessadorPagamentoFactory fabricaPagamentos;
    private final PublicadorEventosPedido publicador;

    public PedidoService(RepositorioProdutos produtos, RepositorioPedidos pedidos,
                         ProcessadorPagamentoFactory fabricaPagamentos,
                         PublicadorEventosPedido publicador) {
        this.produtos = produtos;
        this.pedidos = pedidos;
        this.fabricaPagamentos = fabricaPagamentos;
        this.publicador = publicador;
    }

    /** Caso de uso 1: montar o pedido e calcular o frete com a estratégia escolhida. */
    public Pedido criarPedido(Cliente cliente, Map<Integer, Integer> quantidadePorProduto,
                              EstrategiaFrete estrategiaFrete) {
        List<ItemPedido> itens = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entrada : quantidadePorProduto.entrySet()) {
            Produto produto = produtos.buscarPorId(entrada.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado: " + entrada.getKey()));
            if (entrada.getValue() > produto.getEstoque()) {
                throw new IllegalStateException("Estoque insuficiente para " + produto.getNome()
                        + " (disponivel: " + produto.getEstoque() + ")");
            }
            itens.add(new ItemPedido(produto, entrada.getValue()));
        }

        Pedido pedido = new Pedido(pedidos.proximoId(), cliente, itens);

        CotacaoFrete cotacao = estrategiaFrete.calcular(pedido);          // STRATEGY
        pedido.aplicarFrete(estrategiaFrete.getNome(), cotacao.getValor(), cotacao.getPrazoDias());

        pedidos.salvar(pedido);
        return pedido;
    }

    /** Caso de uso 2: pagar. Só baixa estoque e muda status se o gateway aprovar. */
    public ResultadoPagamento pagar(int pedidoId, DadosPagamento dados) {
        Pedido pedido = buscarPedido(pedidoId);
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new IllegalStateException("Pedido #" + pedidoId + " nao esta aguardando pagamento");
        }
        // Revalida o estoque ANTES de cobrar (outro pedido pode ter comprado o item).
        for (ItemPedido item : pedido.getItens()) {
            if (item.getQuantidade() > item.getProduto().getEstoque()) {
                return ResultadoPagamento.recusado("Estoque esgotado para " + item.getProduto().getNome());
            }
        }

        ProcessadorPagamento processador = fabricaPagamentos.criar(dados.getForma()); // FACTORY
        ResultadoPagamento resultado = processador.processar(pedido, dados);            // ADAPTER

        if (resultado.isAprovado()) {
            for (ItemPedido item : pedido.getItens()) {
                item.getProduto().baixarEstoque(item.getQuantidade());
            }
            pedido.registrarPagamento(dados.getForma().name(), resultado.getCodigoTransacao());
            mudarStatus(pedido, StatusPedido.PAGO);
        }
        return resultado;
    }

    /** Caso de uso 3: despachar o pedido. */
    public void enviar(int pedidoId) {
        mudarStatus(buscarPedido(pedidoId), StatusPedido.ENVIADO);
    }

    /** Caso de uso 4: confirmar entrega. */
    public void confirmarEntrega(int pedidoId) {
        mudarStatus(buscarPedido(pedidoId), StatusPedido.ENTREGUE);
    }

    /** Caso de uso 5: cancelar. Se já estava pago, devolve o estoque. */
    public void cancelar(int pedidoId) {
        Pedido pedido = buscarPedido(pedidoId);
        boolean estavaPago = pedido.getStatus() == StatusPedido.PAGO;
        mudarStatus(pedido, StatusPedido.CANCELADO);
        if (estavaPago) {
            for (ItemPedido item : pedido.getItens()) {
                item.getProduto().devolverEstoque(item.getQuantidade());
            }
        }
    }

    public Pedido buscarPedido(int pedidoId) {
        return pedidos.buscarPorId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado: " + pedidoId));
    }

    public List<Pedido> listarPedidos() {
        return pedidos.listarTodos();
    }

    public List<Produto> listarProdutos() {
        return produtos.listarTodos();
    }

    /** Ponto único de mudança de status: valida a transição e dispara o OBSERVER. */
    private void mudarStatus(Pedido pedido, StatusPedido novoStatus) {
        StatusPedido anterior = pedido.getStatus();
        pedido.alterarStatus(novoStatus);
        publicador.notificar(pedido, anterior);                                         // OBSERVER
    }
}
