package loja.pagamento;

import loja.dominio.Pedido;
import loja.pagamento.externo.CardGatewayClient;
import loja.pagamento.externo.GatewayResponse;

/**
 * ADAPTER concreto: adapta o gateway internacional (CardGatewayClient).
 * Traduções feitas aqui:
 *   - BigDecimal -> String "123.45" + moeda "BRL"
 *   - código HTTP + errorCode em inglês -> ResultadoPagamento com mensagem em português
 */
public class CartaoCreditoAdapter implements ProcessadorPagamento {

    private final CardGatewayClient gateway; // "Adaptee"

    public CartaoCreditoAdapter(CardGatewayClient gateway) {
        this.gateway = gateway;
    }

    @Override
    public ResultadoPagamento processar(Pedido pedido, DadosPagamento dados) {
        String valor = pedido.getTotal().toPlainString();
        GatewayResponse resposta = gateway.charge(dados.getTokenCartao(), valor, "BRL");

        if (resposta.getHttpStatus() == 200) {
            return ResultadoPagamento.aprovado(resposta.getTransactionId());
        }
        return ResultadoPagamento.recusado(traduzirErro(resposta.getErrorCode()));
    }

    private String traduzirErro(String codigo) {
        if ("card_declined".equals(codigo)) return "Cartao recusado pela operadora";
        if ("insufficient_funds".equals(codigo)) return "Limite insuficiente";
        if ("invalid_card_token".equals(codigo)) return "Dados do cartao invalidos";
        if ("unauthorized".equals(codigo)) return "Falha de configuracao do gateway";
        return "Erro desconhecido no gateway: " + codigo;
    }
}
