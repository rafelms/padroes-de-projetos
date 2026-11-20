package loja.pagamento.externo;

/** Objeto de retorno do SDK Pix (formato do banco, não nosso). */
public class CobrancaPix {

    private final String txid;
    private final String situacao; // "CONCLUIDA" ou "REJEITADA"

    public CobrancaPix(String txid, String situacao) {
        this.txid = txid;
        this.situacao = situacao;
    }

    public String getTxid() { return txid; }
    public String getSituacao() { return situacao; }
}
