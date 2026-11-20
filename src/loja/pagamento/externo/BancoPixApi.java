package loja.pagamento.externo;

import java.util.UUID;

/**
 * SIMULA UMA BIBLIOTECA DE TERCEIRO (SDK do banco para Pix).
 * Consideramos que NÃO podemos alterar esta classe: ela tem o "jeito" do banco
 * (valor em centavos, nomes próprios, retorno próprio).
 */
public class BancoPixApi {

    public CobrancaPix criarCobranca(String chaveRecebedor, long valorEmCentavos, String descricao) {
        if (chaveRecebedor == null || chaveRecebedor.isBlank()) {
            throw new IllegalArgumentException("Chave Pix do recebedor obrigatoria");
        }
        if (valorEmCentavos <= 0) {
            return new CobrancaPix(null, "REJEITADA");
        }
        // Na vida real o cliente pagaria pelo app do banco; aqui a confirmação é imediata.
        String txid = "PIX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new CobrancaPix(txid, "CONCLUIDA");
    }
}
