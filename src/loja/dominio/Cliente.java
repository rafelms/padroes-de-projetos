package loja.dominio;

/** Cliente que faz o pedido. Telefone é opcional (pode ser null). */
public class Cliente {

    private final String nome;
    private final String email;
    private final String telefone;

    public Cliente(String nome, String email, String telefone) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }

    public boolean temTelefone() {
        return telefone != null && !telefone.isBlank();
    }

    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }
}
