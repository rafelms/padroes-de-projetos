package loja.frete;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Resultado de um cálculo de frete: valor e prazo. */
public class CotacaoFrete {

    private final BigDecimal valor;
    private final int prazoDias;

    public CotacaoFrete(BigDecimal valor, int prazoDias) {
        this.valor = valor.setScale(2, RoundingMode.HALF_UP);
        this.prazoDias = prazoDias;
    }

    public BigDecimal getValor() { return valor; }
    public int getPrazoDias() { return prazoDias; }
}
