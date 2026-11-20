package loja.exemplos;

import java.math.BigDecimal;
import java.util.List;
import loja.dominio.Cliente;
import loja.dominio.ItemPedido;
import loja.dominio.Pedido;
import loja.dominio.Produto;
import loja.pagamento.CartaoCreditoAdapter;
import loja.pagamento.DadosPagamento;
import loja.pagamento.PixAdapter;
import loja.pagamento.ProcessadorPagamento;
import loja.pagamento.ResultadoPagamento;
import loja.pagamento.externo.BancoPixApi;
import loja.pagamento.externo.CardGatewayClient;
import loja.pagamento.externo.CobrancaPix;
import loja.pagamento.externo.GatewayResponse;

/**
 * EXEMPLO ISOLADO: ADAPTER (pagamento).
 *
 * Parte 1: chama os dois SDKs "de terceiros" DIRETAMENTE, para você ver como as
 *          interfaces são incompatíveis (centavos x texto, "CONCLUIDA" x HTTP 402...).
 * Parte 2: chama os mesmos SDKs através dos adapters. O resultado passa a ter
 *          UM formato só (ResultadoPagamento), em português.
 *
 * Rodar:  executar.bat exemplo adapter     (ou ./executar.sh exemplo adapter)
 */
public class ExemploAdapter {

    public static void main(String[] args) {
        System.out.println("=== Parte 1: os SDKs sem adapter (cada um com seu jeito) ===\n");

        BancoPixApi bancoPix = new BancoPixApi();
        CobrancaPix cobranca = bancoPix.criarCobranca("chave-pix-ficticia", 13490L, "Teste"); // centavos!
        System.out.println("  Pix    -> situacao=" + cobranca.getSituacao() + ", txid=" + cobranca.getTxid());

        CardGatewayClient gateway = new CardGatewayClient("chave-api-ficticia");
        GatewayResponse resposta = gateway.charge("tok_4111111111110000", "134.90", "BRL"); // texto!
        System.out.println("  Cartao -> http=" + resposta.getHttpStatus() + ", error=" + resposta.getErrorCode());

        System.out.println("\n=== Parte 2: os mesmos SDKs atras do contrato ProcessadorPagamento ===\n");

        Produto teclado = new Produto(1, "Teclado mecanico", new BigDecimal("134.90"), 1.2, 10);
        Pedido pedido = new Pedido(1, new Cliente("Cliente Exemplo", "cliente@example.com", null),
                List.of(new ItemPedido(teclado, 1)));

        ProcessadorPagamento pix = new PixAdapter(new BancoPixApi(), "chave-pix-ficticia");
        ProcessadorPagamento cartao = new CartaoCreditoAdapter(new CardGatewayClient("chave-api-ficticia"));

        // A partir daqui, o código é IGUAL para os dois gateways.
        mostrar("Pix", pix.processar(pedido, DadosPagamento.pix()));
        mostrar("Cartao recusado", cartao.processar(pedido, DadosPagamento.cartao("tok_4111111111110000")));
        mostrar("Cartao aprovado", cartao.processar(pedido, DadosPagamento.cartao("tok_4111111111111111")));

        System.out.println("\nRepare: \"card_declined\" virou \"Cartao recusado pela operadora\",");
        System.out.println("e R$ 134,90 virou 13490 centavos dentro do PixAdapter.");
    }

    private static void mostrar(String rotulo, ResultadoPagamento r) {
        System.out.printf("  %-16s -> %s | %s%s%n", rotulo,
                r.isAprovado() ? "APROVADO" : "RECUSADO", r.getMensagem(),
                r.getCodigoTransacao() != null ? " | " + r.getCodigoTransacao() : "");
    }
}
