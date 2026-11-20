package loja.frete;

import loja.dominio.Pedido;

/**
 * PADRÃO COMPORTAMENTAL: STRATEGY (interface da estratégia).
 *
 * Cada modalidade de entrega tem sua própria regra de cálculo. O serviço de
 * pedidos não sabe qual regra está usando: só chama calcular().
 */
public interface EstrategiaFrete {

    String getNome();

    CotacaoFrete calcular(Pedido pedido);
}
