package loja;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import loja.aplicacao.PedidoService;
import loja.dominio.Cliente;
import loja.dominio.Pedido;
import loja.dominio.StatusPedido;
import loja.frete.FretePac;
import loja.frete.FreteSedex;
import loja.frete.RetiradaNaLoja;
import loja.notificacao.PublicadorEventosPedido;
import loja.notificacao.RegistroAuditoria;
import loja.pagamento.DadosPagamento;
import loja.pagamento.ProcessadorPagamentoFactory;
import loja.pagamento.ResultadoPagamento;
import loja.repositorio.RepositorioPedidos;
import loja.repositorio.RepositorioProdutos;

/**
 * Testes automatizados simples, sem framework (o projeto não depende de Maven/JUnit).
 * Cada teste monta um sistema "limpo" e verifica um comportamento.
 */
public class TestesSistema {

    private static int aprovados = 0;
    private static int falhas = 0;

    private RepositorioProdutos produtos;
    private RegistroAuditoria auditoria;
    private PublicadorEventosPedido publicador;
    private PedidoService servico;
    private final Cliente cliente = new Cliente("Teste", "teste@example.com", null);

    private void montar() {
        produtos = RepositorioProdutos.comCatalogoInicial();
        auditoria = new RegistroAuditoria();
        publicador = new PublicadorEventosPedido();
        publicador.inscrever(auditoria);
        servico = new PedidoService(produtos, new RepositorioPedidos(),
                new ProcessadorPagamentoFactory("chave-pix-ficticia", "chave-api-ficticia"), publicador);
    }

    private Map<Integer, Integer> carrinho(int produtoId, int qtd) {
        Map<Integer, Integer> c = new LinkedHashMap<>();
        c.put(produtoId, qtd);
        return c;
    }

    // ----------------------------------------------------------- STRATEGY
    void freteSedexCalculaPorPeso() {
        montar();
        Pedido p = servico.criarPedido(cliente, carrinho(2, 2), new FreteSedex()); // 0.6kg
        verificar(new BigDecimal("23.00").compareTo(p.getValorFrete()) == 0, "SEDEX = 20 + 5*0.6 = 23.00");
    }

    void fretePacGratisAcimaDe300() {
        montar();
        Pedido p = servico.criarPedido(cliente, carrinho(3, 1), new FretePac()); // 899.00
        verificar(p.getValorFrete().signum() == 0, "PAC gratis para subtotal >= 300");
    }

    void retiradaNaLojaNaoCobra() {
        montar();
        Pedido p = servico.criarPedido(cliente, carrinho(3, 1), new RetiradaNaLoja());
        verificar(p.getTotal().compareTo(new BigDecimal("899.00")) == 0, "retirada: total = subtotal");
    }

    // -------------------------------------------------- FACTORY + ADAPTER
    void pixAprovaBaixaEstoqueENotifica() {
        montar();
        Pedido p = servico.criarPedido(cliente, carrinho(1, 2), new FretePac());
        ResultadoPagamento r = servico.pagar(p.getId(), DadosPagamento.pix());
        verificar(r.isAprovado() && r.getCodigoTransacao().startsWith("PIX-"), "pix aprovado com txid");
        verificar(p.getStatus() == StatusPedido.PAGO, "status PAGO apos pix");
        verificar(produtos.buscarPorId(1).get().getEstoque() == 8, "estoque baixou de 10 para 8");
        verificar(auditoria.getRegistros().size() == 1, "observer de auditoria foi notificado");
    }

    void cartaoRecusadoNaoAlteraNada() {
        montar();
        Pedido p = servico.criarPedido(cliente, carrinho(1, 1), new FretePac());
        ResultadoPagamento r = servico.pagar(p.getId(), DadosPagamento.cartao("tok_0000"));
        verificar(!r.isAprovado(), "cartao final 0000 recusado");
        verificar(r.getMensagem().equals("Cartao recusado pela operadora"), "erro traduzido pelo adapter");
        verificar(p.getStatus() == StatusPedido.AGUARDANDO_PAGAMENTO, "status inalterado");
        verificar(produtos.buscarPorId(1).get().getEstoque() == 10, "estoque inalterado");
        verificar(auditoria.getRegistros().isEmpty(), "nenhuma notificacao");
    }

    void cartaoSemLimiteAcimaDe5000() {
        montar();
        Map<Integer, Integer> c = carrinho(3, 5); // 5 monitores = 4495.00
        c.put(1, 4);                              // 4 teclados  =  999.60 -> acima de 5000
        Pedido p = servico.criarPedido(cliente, c, new FreteSedex());
        ResultadoPagamento r = servico.pagar(p.getId(), DadosPagamento.cartao("tok_1234"));
        verificar(!r.isAprovado() && r.getMensagem().equals("Limite insuficiente"), "limite insuficiente");
    }

    // ----------------------------------------------------------- OBSERVER
    void observerComFalhaNaoQuebraOsOutros() {
        montar();
        publicador.inscrever((pedido, anterior) -> { throw new RuntimeException("servidor SMS fora"); });
        RegistroAuditoria segundo = new RegistroAuditoria();
        publicador.inscrever(segundo);
        Pedido p = servico.criarPedido(cliente, carrinho(2, 1), new FretePac());
        servico.pagar(p.getId(), DadosPagamento.pix());
        verificar(p.getStatus() == StatusPedido.PAGO, "pedido pago mesmo com observer quebrado");
        verificar(segundo.getRegistros().size() == 1, "observer seguinte ainda foi chamado");
    }

    // --------------------------------------------------- REGRAS DE NEGOCIO
    void transicaoInvalidaEBloqueada() {
        montar();
        Pedido p = servico.criarPedido(cliente, carrinho(2, 1), new FretePac());
        try {
            servico.enviar(p.getId()); // enviar sem pagar
            verificar(false, "deveria bloquear envio sem pagamento");
        } catch (IllegalStateException e) {
            verificar(true, "envio sem pagamento bloqueado");
        }
    }

    void cancelarPagoDevolveEstoque() {
        montar();
        Pedido p = servico.criarPedido(cliente, carrinho(5, 2), new FretePac());
        servico.pagar(p.getId(), DadosPagamento.pix());
        verificar(produtos.buscarPorId(5).get().getEstoque() == 0, "headset esgotado apos pagamento");
        servico.cancelar(p.getId());
        verificar(produtos.buscarPorId(5).get().getEstoque() == 2, "estoque devolvido ao cancelar");
    }

    void estoqueInsuficienteImpedeCriacao() {
        montar();
        try {
            servico.criarPedido(cliente, carrinho(5, 3), new FretePac());
            verificar(false, "deveria bloquear estoque insuficiente");
        } catch (IllegalStateException e) {
            verificar(true, "estoque insuficiente bloqueado");
        }
    }

    // ------------------------------------------------------------- runner
    private static void verificar(boolean condicao, String descricao) {
        if (condicao) {
            aprovados++;
            System.out.println("  [OK]    " + descricao);
        } else {
            falhas++;
            System.out.println("  [FALHA] " + descricao);
        }
    }

    public static void main(String[] args) {
        TestesSistema t = new TestesSistema();
        System.out.println("Strategy:");
        t.freteSedexCalculaPorPeso();
        t.fretePacGratisAcimaDe300();
        t.retiradaNaLojaNaoCobra();
        System.out.println("Factory + Adapter:");
        t.pixAprovaBaixaEstoqueENotifica();
        t.cartaoRecusadoNaoAlteraNada();
        t.cartaoSemLimiteAcimaDe5000();
        System.out.println("Observer:");
        t.observerComFalhaNaoQuebraOsOutros();
        System.out.println("Regras de negocio:");
        t.transicaoInvalidaEBloqueada();
        t.cancelarPagoDevolveEstoque();
        t.estoqueInsuficienteImpedeCriacao();

        System.out.println("\nResultado: " + aprovados + " verificacoes OK, " + falhas + " falha(s)");
        if (falhas > 0) {
            System.exit(1);
        }
    }
}
