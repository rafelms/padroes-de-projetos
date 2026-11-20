package loja.notificacao;

import loja.dominio.Pedido;
import loja.dominio.StatusPedido;

/**
 * PADRÃO COMPORTAMENTAL: OBSERVER (interface do observador).
 *
 * Qualquer parte do sistema que queira "reagir" quando um pedido muda de
 * status implementa esta interface e se inscreve no PublicadorEventosPedido.
 */
public interface ObservadorPedido {

    void statusAlterado(Pedido pedido, StatusPedido statusAnterior);
}
