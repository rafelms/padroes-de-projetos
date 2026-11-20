package loja.dominio;

/**
 * Ciclo de vida do pedido.
 *
 * Decisão consciente: NÃO usamos o padrão State aqui. São só 5 estados e as
 * transições são uma regra simples ("de X pode ir para Y"), sem comportamento
 * diferente por estado. Um switch dentro do enum resolve com muito menos classes.
 */
public enum StatusPedido {
    AGUARDANDO_PAGAMENTO,
    PAGO,
    ENVIADO,
    ENTREGUE,
    CANCELADO;

    public boolean podeMudarPara(StatusPedido novo) {
        switch (this) {
            case AGUARDANDO_PAGAMENTO:
                return novo == PAGO || novo == CANCELADO;
            case PAGO:
                return novo == ENVIADO || novo == CANCELADO;
            case ENVIADO:
                return novo == ENTREGUE;
            default:
                return false; // ENTREGUE e CANCELADO são estados finais
        }
    }
}
