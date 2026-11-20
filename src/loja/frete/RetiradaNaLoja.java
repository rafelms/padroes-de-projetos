package loja.frete;

import java.math.BigDecimal;
import loja.dominio.Pedido;

/** STRATEGY concreta: cliente retira na loja. Sem custo, disponível em 1 dia. */
public class RetiradaNaLoja implements EstrategiaFrete {

    @Override
    public String getNome() {
        return "RETIRADA NA LOJA";
    }

    @Override
    public CotacaoFrete calcular(Pedido pedido) {
        return new CotacaoFrete(BigDecimal.ZERO, 1);
    }
}
