package loja.exemplos;

import java.math.BigDecimal;
import java.util.List;
import loja.dominio.Cliente;
import loja.dominio.ItemPedido;
import loja.dominio.Pedido;
import loja.dominio.Produto;
import loja.pagamento.DadosPagamento;
import loja.pagamento.FormaPagamento;
import loja.pagamento.ProcessadorPagamento;
import loja.pagamento.ProcessadorPagamentoFactory;
import loja.pagamento.ResultadoPagamento;

/**
 * EXEMPLO ISOLADO: FACTORY (criação do processador de pagamento).
 *
 * Mostra que:
 *   1. Quem usa a fábrica só recebe a interface ProcessadorPagamento.
 *   2. As credenciais ficam num lugar só: a fábrica. Uma fábrica mal configurada
 *      (sem API key) afeta o cartão, e o erro chega traduzido pelo adapter.
 *
 * Os valores de credencial são FICTÍCIOS. Num sistema real eles viriam de
 * variáveis de ambiente ou de um cofre de segredos, nunca do código-fonte.
 *
 * Rodar:  executar.bat exemplo factory     (ou ./executar.sh exemplo factory)
 */
public class ExemploFactory {

    public static void main(String[] args) {
        Produto mouse = new Produto(1, "Mouse sem fio", new BigDecimal("89.90"), 0.3, 10);
        Pedido pedido = new Pedido(1, new Cliente("Cliente Exemplo", "cliente@example.com", null),
                List.of(new ItemPedido(mouse, 1)));

        ProcessadorPagamentoFactory fabrica =
                new ProcessadorPagamentoFactory("chave-pix-ficticia", "chave-api-ficticia");

        System.out.println("=== Fabrica configurada ===");
        for (FormaPagamento forma : FormaPagamento.values()) {
            ProcessadorPagamento processador = fabrica.criar(forma);
            System.out.println("  " + forma + " -> " + processador.getClass().getSimpleName()
                    + " (o chamador so enxerga ProcessadorPagamento)");
        }

        DadosPagamento cartao = DadosPagamento.cartao("tok_4111111111111111");
        mostrar("Cartao", fabrica.criar(FormaPagamento.CARTAO_CREDITO).processar(pedido, cartao));

        System.out.println("\n=== Fabrica SEM API key do cartao ===");
        ProcessadorPagamentoFactory semChave = new ProcessadorPagamentoFactory("chave-pix-ficticia", "");
        mostrar("Pix", semChave.criar(FormaPagamento.PIX).processar(pedido, DadosPagamento.pix()));
        mostrar("Cartao", semChave.criar(FormaPagamento.CARTAO_CREDITO).processar(pedido, cartao));

        System.out.println("\nExercicio: crie FormaPagamento.BOLETO, um BoletoAdapter e um case na");
        System.out.println("fabrica. Confira que o PedidoService nao precisa mudar.");
    }

    private static void mostrar(String rotulo, ResultadoPagamento r) {
        System.out.printf("  %-7s -> %s | %s%n", rotulo,
                r.isAprovado() ? "APROVADO" : "RECUSADO", r.getMensagem());
    }
}
