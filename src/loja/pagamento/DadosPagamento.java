package loja.pagamento;

/** O que o cliente informou para pagar. tokenCartao só é usado no cartão. */
public class DadosPagamento {

    private final FormaPagamento forma;
    private final String tokenCartao;

    private DadosPagamento(FormaPagamento forma, String tokenCartao) {
        this.forma = forma;
        this.tokenCartao = tokenCartao;
    }

    public static DadosPagamento pix() {
        return new DadosPagamento(FormaPagamento.PIX, null);
    }

    public static DadosPagamento cartao(String tokenCartao) {
        return new DadosPagamento(FormaPagamento.CARTAO_CREDITO, tokenCartao);
    }

    public FormaPagamento getForma() { return forma; }
    public String getTokenCartao() { return tokenCartao; }
}
