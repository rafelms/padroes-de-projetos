package loja.pagamento.externo;

import java.util.UUID;

/**
 * SIMULA UMA BIBLIOTECA DE TERCEIRO (gateway internacional de cartão).
 * Interface totalmente diferente da do Pix: nomes em inglês, valor como String,
 * resposta com código HTTP. Também NÃO podemos alterá-la.
 *
 * Regras da simulação:
 *  - token terminado em "0000"  -> cartão recusado (402 card_declined)
 *  - valor acima de 5000.00     -> sem limite      (402 insufficient_funds)
 */
public class CardGatewayClient {

    private final String apiKey;

    public CardGatewayClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public GatewayResponse charge(String cardToken, String amount, String currency) {
        if (apiKey == null || apiKey.isBlank()) {
            return new GatewayResponse(401, null, "unauthorized");
        }
        if (cardToken == null || cardToken.isBlank()) {
            return new GatewayResponse(400, null, "invalid_card_token");
        }
        if (cardToken.endsWith("0000")) {
            return new GatewayResponse(402, null, "card_declined");
        }
        if (Double.parseDouble(amount) > 5000.00) {
            return new GatewayResponse(402, null, "insufficient_funds");
        }
        String id = "ch_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return new GatewayResponse(200, id, null);
    }
}
