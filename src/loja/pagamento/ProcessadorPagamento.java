package loja.pagamento;

import loja.dominio.Pedido;

/**
 * PADRÃO ESTRUTURAL: ADAPTER (interface "Target").
 *
 * É a interface que o NOSSO sistema espera. Cada gateway externo tem uma API
 * diferente; um adapter por gateway traduz para este contrato único.
 */
public interface ProcessadorPagamento {

    ResultadoPagamento processar(Pedido pedido, DadosPagamento dados);
}
