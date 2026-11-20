package loja.pagamento.externo;

/** Objeto de retorno do gateway de cartão (formato do fornecedor). */
public class GatewayResponse {

    private final int httpStatus;
    private final String transactionId;
    private final String errorCode;

    public GatewayResponse(int httpStatus, String transactionId, String errorCode) {
        this.httpStatus = httpStatus;
        this.transactionId = transactionId;
        this.errorCode = errorCode;
    }

    public int getHttpStatus() { return httpStatus; }
    public String getTransactionId() { return transactionId; }
    public String getErrorCode() { return errorCode; }
}
