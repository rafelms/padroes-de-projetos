package loja.exemplos;

import java.math.BigDecimal;
import java.util.Map;
import loja.aplicacao.PedidoService;
import loja.dominio.Cliente;
import loja.dominio.Pedido;
import loja.dominio.StatusPedido;
import loja.frete.RetiradaNaLoja;
import loja.notificacao.NotificadorEmail;
import loja.notificacao.ObservadorPedido;
import loja.notificacao.PublicadorEventosPedido;
import loja.pagamento.DadosPagamento;
import loja.pagamento.ProcessadorPagamentoFactory;
import loja.repositorio.RepositorioPedidos;
import loja.repositorio.RepositorioProdutos;

/**
 * EXEMPLO ISOLADO: OBSERVER (notificações).
 *
 * Mostra que:
 *   1. Dá para criar um observador NOVO (um "painel" que soma o faturamento)
 *      e inscrevê-lo sem tocar no PedidoService.
 *   2. Se um observador lançar exceção, os outros continuam sendo avisados
 *      e o pedido continua sendo pago (try/catch no publicador).
 *
 * Rodar:  executar.bat exemplo observer     (ou ./executar.sh exemplo observer)
 */
public class ExemploObserver {

    /** Observador novo, criado só neste exemplo. */
    static class PainelFaturamento implements ObservadorPedido {
        private BigDecimal total = BigDecimal.ZERO;

        @Override
        public void statusAlterado(Pedido pedido, StatusPedido statusAnterior) {
            if (pedido.getStatus() == StatusPedido.PAGO) {
                total = total.add(pedido.getTotal());
                System.out.println("  [PAINEL] faturamento acumulado: R$ " + total);
            }
        }
    }

    /** Observador que sempre falha, para provar o isolamento. */
    static class ObservadorQuebrado implements ObservadorPedido {
        @Override
        public void statusAlterado(Pedido pedido, StatusPedido statusAnterior) {
            throw new RuntimeException("servico de SMS fora do ar (simulado)");
        }
    }

    public static void main(String[] args) {
        PublicadorEventosPedido publicador = new PublicadorEventosPedido();
        publicador.inscrever(new NotificadorEmail());
        publicador.inscrever(new ObservadorQuebrado()); // falha no meio da lista
        publicador.inscrever(new PainelFaturamento());  // mesmo assim é avisado

        // Mesma montagem do Main, só com observadores diferentes.
        PedidoService servico = new PedidoService(
                RepositorioProdutos.comCatalogoInicial(),
                new RepositorioPedidos(),
                new ProcessadorPagamentoFactory("chave-pix-ficticia", "chave-api-ficticia"),
                publicador);

        Cliente cliente = new Cliente("Cliente Exemplo", "cliente@example.com", null);

        System.out.println("Pagando dois pedidos:\n");
        for (int produtoId : new int[] {2, 4}) {
            Pedido pedido = servico.criarPedido(cliente, Map.of(produtoId, 1), new RetiradaNaLoja());
            servico.pagar(pedido.getId(), DadosPagamento.pix());
            System.out.println("  -> pedido #" + pedido.getId() + " ficou "
                    + servico.buscarPedido(pedido.getId()).getStatus() + "\n");
        }

        System.out.println("O PedidoService nao sabe que o painel existe: ele so chama");
        System.out.println("publicador.notificar(...) dentro de mudarStatus().");
    }
}
