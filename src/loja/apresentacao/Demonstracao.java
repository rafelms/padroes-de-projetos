package loja.apresentacao;

import java.util.LinkedHashMap;
import java.util.Map;
import loja.aplicacao.PedidoService;
import loja.dominio.Cliente;
import loja.dominio.Pedido;
import loja.frete.FretePac;
import loja.frete.FreteSedex;
import loja.frete.RetiradaNaLoja;
import loja.notificacao.RegistroAuditoria;
import loja.pagamento.DadosPagamento;
import loja.pagamento.ResultadoPagamento;

/** Roteiro automático para a apresentação: mostra cada padrão em ação, em ordem. */
public class Demonstracao {

    private final PedidoService servico;
    private final RegistroAuditoria auditoria;

    public Demonstracao(PedidoService servico, RegistroAuditoria auditoria) {
        this.servico = servico;
        this.auditoria = auditoria;
    }

    public void executar() {
        Cliente ana = new Cliente("Ana Souza", "ana@example.com", "(00) 90000-0001");
        Cliente bruno = new Cliente("Bruno Lima", "bruno@example.com", null); // sem telefone

        titulo("CATALOGO");
        servico.listarProdutos().forEach(p -> System.out.println(Formatador.produto(p)));

        // ---------------------------------------------------------------
        titulo("1) STRATEGY - mesmo carrinho, tres estrategias de frete");
        Map<Integer, Integer> carrinhoPequeno = new LinkedHashMap<>();
        carrinhoPequeno.put(2, 1); // mouse
        carrinhoPequeno.put(4, 2); // 2 cabos

        Pedido p1 = servico.criarPedido(ana, carrinhoPequeno, new FretePac());
        Pedido p2 = servico.criarPedido(ana, carrinhoPequeno, new FreteSedex());
        Pedido p3 = servico.criarPedido(ana, carrinhoPequeno, new RetiradaNaLoja());
        System.out.println(Formatador.pedido(p1));
        System.out.println(Formatador.pedido(p2));
        System.out.println(Formatador.pedido(p3));

        System.out.println("\n  Carrinho acima de R$ 300 com PAC -> regra de frete gratis da estrategia PAC:");
        Map<Integer, Integer> carrinhoGrande = new LinkedHashMap<>();
        carrinhoGrande.put(1, 1); // teclado
        carrinhoGrande.put(2, 1); // mouse
        Pedido p4 = servico.criarPedido(bruno, carrinhoGrande, new FretePac());
        System.out.println(Formatador.pedido(p4));

        // ---------------------------------------------------------------
        titulo("2) FACTORY + ADAPTER - pagando com gateways diferentes");
        System.out.println("  Pedido #" + p1.getId() + " via PIX (SDK do banco, valor em centavos):");
        mostrar(servico.pagar(p1.getId(), DadosPagamento.pix()));

        System.out.println("\n  Pedido #" + p4.getId() + " via CARTAO com token recusado (final 0000):");
        mostrar(servico.pagar(p4.getId(), DadosPagamento.cartao("tok_4111111111110000")));
        System.out.println("  -> status continua: " + servico.buscarPedido(p4.getId()).getStatus()
                + " (nada foi notificado, estoque intacto)");

        System.out.println("\n  Pedido #" + p4.getId() + " via CARTAO com token valido:");
        mostrar(servico.pagar(p4.getId(), DadosPagamento.cartao("tok_4111111111111111")));

        // ---------------------------------------------------------------
        titulo("3) OBSERVER - ciclo de vida dispara notificacoes");
        System.out.println("  Enviando pedido #" + p1.getId() + " (Ana TEM telefone -> email + SMS):");
        servico.enviar(p1.getId());
        System.out.println("  Enviando pedido #" + p4.getId() + " (Bruno NAO tem telefone -> so email):");
        servico.enviar(p4.getId());
        System.out.println("  Confirmando entrega do pedido #" + p1.getId() + ":");
        servico.confirmarEntrega(p1.getId());
        System.out.println("  Cancelando pedido #" + p2.getId() + " (nao pago):");
        servico.cancelar(p2.getId());

        // ---------------------------------------------------------------
        titulo("4) REGRAS DE NEGOCIO protegidas");
        try {
            System.out.println("  Tentando cancelar o pedido #" + p1.getId() + " que ja foi ENTREGUE...");
            servico.cancelar(p1.getId());
        } catch (IllegalStateException e) {
            System.out.println("  BLOQUEADO: " + e.getMessage());
        }
        try {
            System.out.println("  Tentando comprar 3 headsets (so ha 2 em estoque)...");
            Map<Integer, Integer> c = new LinkedHashMap<>();
            c.put(5, 3);
            servico.criarPedido(ana, c, new FreteSedex());
        } catch (IllegalStateException e) {
            System.out.println("  BLOQUEADO: " + e.getMessage());
        }

        // ---------------------------------------------------------------
        titulo("TRILHA DE AUDITORIA (observer que guarda historico)");
        auditoria.getRegistros().forEach(r -> System.out.println("  " + r));

        titulo("ESTOQUE FINAL");
        servico.listarProdutos().forEach(p -> System.out.println(Formatador.produto(p)));
    }

    private void mostrar(ResultadoPagamento r) {
        System.out.println("  Resultado: " + (r.isAprovado() ? "APROVADO" : "RECUSADO")
                + " - " + r.getMensagem()
                + (r.getCodigoTransacao() != null ? " (transacao " + r.getCodigoTransacao() + ")" : ""));
    }

    private void titulo(String texto) {
        System.out.println("\n==================================================================");
        System.out.println(" " + texto);
        System.out.println("==================================================================");
    }
}
