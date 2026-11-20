package loja.notificacao;

import loja.dominio.Pedido;
import loja.dominio.StatusPedido;

/** OBSERVER concreto: envia e-mail ao cliente a cada mudança de status (simulado no console). */
public class NotificadorEmail implements ObservadorPedido {

    @Override
    public void statusAlterado(Pedido pedido, StatusPedido statusAnterior) {
        System.out.println("  [EMAIL -> " + pedido.getCliente().getEmail() + "] "
                + "Ola " + pedido.getCliente().getNome() + ", seu pedido #" + pedido.getId()
                + " agora esta: " + pedido.getStatus());
    }
}
