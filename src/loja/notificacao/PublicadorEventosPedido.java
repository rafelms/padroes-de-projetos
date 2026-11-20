package loja.notificacao;

import java.util.ArrayList;
import java.util.List;
import loja.dominio.Pedido;
import loja.dominio.StatusPedido;

/**
 * OBSERVER: o "Subject" (quem publica o evento).
 *
 * Mantém a lista de observadores e avisa todos quando um pedido muda de status.
 * Cada observador é chamado dentro de try/catch: se o SMS falhar, o e-mail e a
 * auditoria continuam funcionando e o pedido NÃO deixa de ser pago.
 */
public class PublicadorEventosPedido {

    private final List<ObservadorPedido> observadores = new ArrayList<>();

    public void inscrever(ObservadorPedido observador) {
        observadores.add(observador);
    }

    public void remover(ObservadorPedido observador) {
        observadores.remove(observador);
    }

    public void notificar(Pedido pedido, StatusPedido statusAnterior) {
        for (ObservadorPedido observador : observadores) {
            try {
                observador.statusAlterado(pedido, statusAnterior);
            } catch (RuntimeException e) {
                System.out.println("  [AVISO] Falha em " + observador.getClass().getSimpleName()
                        + ": " + e.getMessage());
            }
        }
    }
}
