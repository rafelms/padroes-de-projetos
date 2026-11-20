package loja.pagamento;

import java.math.BigDecimal;
import loja.dominio.Pedido;
import loja.pagamento.externo.BancoPixApi;
import loja.pagamento.externo.CobrancaPix;

/**
 * ADAPTER concreto: adapta o SDK do banco (BancoPixApi) para ProcessadorPagamento.
 * Traduções feitas aqui:
 *   - BigDecimal em reais  -> long em centavos
 *   - CobrancaPix("CONCLUIDA") -> ResultadoPagamento.aprovado
 */
public class PixAdapter implements ProcessadorPagamento {

    private final BancoPixApi api;          // "Adaptee"
    private final String chavePixDaLoja;

    public PixAdapter(BancoPixApi api, String chavePixDaLoja) {
        this.api = api;
        this.chavePixDaLoja = chavePixDaLoja;
    }

    @Override
    public ResultadoPagamento processar(Pedido pedido, DadosPagamento dados) {
        long centavos = pedido.getTotal().multiply(new BigDecimal("100")).longValue();
        CobrancaPix cobranca = api.criarCobranca(chavePixDaLoja, centavos, "Pedido #" + pedido.getId());

        if ("CONCLUIDA".equals(cobranca.getSituacao())) {
            return ResultadoPagamento.aprovado(cobranca.getTxid());
        }
        return ResultadoPagamento.recusado("Cobranca Pix rejeitada pelo banco");
    }
}
