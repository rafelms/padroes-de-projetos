package loja.pagamento;

/** Resultado padronizado de pagamento: o MESMO formato para qualquer gateway. */
public class ResultadoPagamento {

    private final boolean aprovado;
    private final String codigoTransacao;
    private final String mensagem;

    private ResultadoPagamento(boolean aprovado, String codigoTransacao, String mensagem) {
        this.aprovado = aprovado;
        this.codigoTransacao = codigoTransacao;
        this.mensagem = mensagem;
    }

    public static ResultadoPagamento aprovado(String codigoTransacao) {
        return new ResultadoPagamento(true, codigoTransacao, "Pagamento aprovado");
    }

    public static ResultadoPagamento recusado(String motivo) {
        return new ResultadoPagamento(false, null, motivo);
    }

    public boolean isAprovado() { return aprovado; }
    public String getCodigoTransacao() { return codigoTransacao; }
    public String getMensagem() { return mensagem; }
}
