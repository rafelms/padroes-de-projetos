package loja.pagamento;

import loja.pagamento.externo.BancoPixApi;
import loja.pagamento.externo.CardGatewayClient;

/**
 * PADRÃO CRIACIONAL: FACTORY (Simple Factory).
 *
 * Centraliza COMO cada processador é montado: qual SDK instanciar, com qual
 * credencial (chave Pix, API key). O serviço de pedidos só pede
 * "me dê o processador de PIX" e recebe a interface ProcessadorPagamento.
 *
 * Para aceitar uma nova forma de pagamento: novo enum + novo adapter + um case aqui.
 * Nada muda no PedidoService.
 */
public class ProcessadorPagamentoFactory {

    private final String chavePixDaLoja;
    private final String apiKeyGatewayCartao;

    public ProcessadorPagamentoFactory(String chavePixDaLoja, String apiKeyGatewayCartao) {
        this.chavePixDaLoja = chavePixDaLoja;
        this.apiKeyGatewayCartao = apiKeyGatewayCartao;
    }

    public ProcessadorPagamento criar(FormaPagamento forma) {
        switch (forma) {
            case PIX:
                return new PixAdapter(new BancoPixApi(), chavePixDaLoja);
            case CARTAO_CREDITO:
                return new CartaoCreditoAdapter(new CardGatewayClient(apiKeyGatewayCartao));
            default:
                throw new IllegalArgumentException("Forma de pagamento nao suportada: " + forma);
        }
    }
}
