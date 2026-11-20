package loja.notificacao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import loja.dominio.Pedido;
import loja.dominio.StatusPedido;

/** OBSERVER concreto: guarda o histórico de todas as mudanças de status (trilha de auditoria). */
public class RegistroAuditoria implements ObservadorPedido {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final List<String> registros = new ArrayList<>();

    @Override
    public void statusAlterado(Pedido pedido, StatusPedido statusAnterior) {
        registros.add(LocalDateTime.now().format(FORMATO) + " | Pedido #" + pedido.getId()
                + " | " + statusAnterior + " -> " + pedido.getStatus());
    }

    public List<String> getRegistros() {
        return Collections.unmodifiableList(registros);
    }
}
