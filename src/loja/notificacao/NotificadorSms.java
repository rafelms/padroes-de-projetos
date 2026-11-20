package loja.notificacao;

import loja.dominio.Pedido;
import loja.dominio.StatusPedido;

/**
 * OBSERVER concreto: SMS só para eventos de entrega (ENVIADO/ENTREGUE),
 * e só se o cliente cadastrou telefone. Cada observador decide o que lhe interessa.
 */
public class NotificadorSms implements ObservadorPedido {

    @Override
    public void statusAlterado(Pedido pedido, StatusPedido statusAnterior) {
        if (!pedido.getCliente().temTelefone()) {
            return;
        }
        StatusPedido atual = pedido.getStatus();
        if (atual == StatusPedido.ENVIADO || atual == StatusPedido.ENTREGUE) {
            System.out.println("  [SMS -> " + pedido.getCliente().getTelefone() + "] "
                    + "Pedido #" + pedido.getId() + ": " + atual);
        }
    }
}
