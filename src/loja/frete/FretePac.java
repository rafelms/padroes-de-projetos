package loja.frete;

import java.math.BigDecimal;
import loja.dominio.Pedido;

/**
 * STRATEGY concreta: PAC (econômico).
 * Regra: R$ 12,00 + R$ 2,50 por kg, 7 dias. Grátis para compras a partir de R$ 300,00.
 * A regra de "frete grátis" pertence só ao PAC, então fica encapsulada aqui.
 */
public class FretePac implements EstrategiaFrete {

    private static final BigDecimal VALOR_BASE = new BigDecimal("12.00");
    private static final BigDecimal VALOR_POR_KG = new BigDecimal("2.50");
    private static final BigDecimal MINIMO_FRETE_GRATIS = new BigDecimal("300.00");

    @Override
    public String getNome() {
        return "PAC";
    }

    @Override
    public CotacaoFrete calcular(Pedido pedido) {
        if (pedido.getSubtotal().compareTo(MINIMO_FRETE_GRATIS) >= 0) {
            return new CotacaoFrete(BigDecimal.ZERO, 7);
        }
        BigDecimal peso = BigDecimal.valueOf(pedido.getPesoTotalKg());
        return new CotacaoFrete(VALOR_BASE.add(VALOR_POR_KG.multiply(peso)), 7);
    }
}
