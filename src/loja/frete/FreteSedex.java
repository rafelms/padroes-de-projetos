package loja.frete;

import java.math.BigDecimal;
import loja.dominio.Pedido;

/**
 * STRATEGY concreta: SEDEX (expresso).
 * Regra: R$ 20,00 + R$ 5,00 por kg, 2 dias. Nunca é grátis.
 */
public class FreteSedex implements EstrategiaFrete {

    private static final BigDecimal VALOR_BASE = new BigDecimal("20.00");
    private static final BigDecimal VALOR_POR_KG = new BigDecimal("5.00");

    @Override
    public String getNome() {
        return "SEDEX";
    }

    @Override
    public CotacaoFrete calcular(Pedido pedido) {
        BigDecimal peso = BigDecimal.valueOf(pedido.getPesoTotalKg());
        return new CotacaoFrete(VALOR_BASE.add(VALOR_POR_KG.multiply(peso)), 2);
    }
}
