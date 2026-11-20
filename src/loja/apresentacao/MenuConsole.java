package loja.apresentacao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import loja.aplicacao.PedidoService;
import loja.dominio.Cliente;
import loja.dominio.Pedido;
import loja.frete.EstrategiaFrete;
import loja.frete.FretePac;
import loja.frete.FreteSedex;
import loja.frete.RetiradaNaLoja;
import loja.notificacao.RegistroAuditoria;
import loja.pagamento.DadosPagamento;
import loja.pagamento.ResultadoPagamento;

/** Interface de linha de comando interativa. Só lê entrada e chama o serviço. */
public class MenuConsole {

    private final PedidoService servico;
    private final RegistroAuditoria auditoria;
    private final Scanner entrada = new Scanner(System.in);

    /** Estratégias disponíveis para o usuário escolher (o "cliente" do Strategy). */
    private final List<EstrategiaFrete> estrategiasFrete =
            List.of(new FretePac(), new FreteSedex(), new RetiradaNaLoja());

    public MenuConsole(PedidoService servico, RegistroAuditoria auditoria) {
        this.servico = servico;
        this.auditoria = auditoria;
    }

    public void iniciar() {
        while (true) {
            System.out.println("\n===== LOJA - SISTEMA DE PEDIDOS =====");
            System.out.println("1 - Listar produtos");
            System.out.println("2 - Criar pedido");
            System.out.println("3 - Pagar pedido");
            System.out.println("4 - Enviar pedido");
            System.out.println("5 - Confirmar entrega");
            System.out.println("6 - Cancelar pedido");
            System.out.println("7 - Listar pedidos");
            System.out.println("8 - Ver auditoria");
            System.out.println("0 - Sair");
            int opcao = lerInt("Opcao: ");
            try {
                switch (opcao) {
                    case 1: servico.listarProdutos().forEach(p -> System.out.println(Formatador.produto(p))); break;
                    case 2: criarPedido(); break;
                    case 3: pagar(); break;
                    case 4: servico.enviar(lerInt("ID do pedido: ")); break;
                    case 5: servico.confirmarEntrega(lerInt("ID do pedido: ")); break;
                    case 6: servico.cancelar(lerInt("ID do pedido: ")); break;
                    case 7: listarPedidos(); break;
                    case 8: auditoria.getRegistros().forEach(r -> System.out.println("  " + r)); break;
                    case 0: System.out.println("Ate logo!"); return;
                    default: System.out.println("Opcao invalida");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("ERRO: " + e.getMessage());
            }
        }
    }

    private void criarPedido() {
        String nome = lerTexto("Nome do cliente: ");
        String email = lerTexto("E-mail: ");
        String telefone = lerTexto("Telefone (enter para pular): ");
        Cliente cliente = new Cliente(nome, email, telefone.isBlank() ? null : telefone);

        Map<Integer, Integer> carrinho = new LinkedHashMap<>();
        servico.listarProdutos().forEach(p -> System.out.println(Formatador.produto(p)));
        while (true) {
            int id = lerInt("ID do produto (0 para finalizar): ");
            if (id == 0) break;
            int qtd = lerInt("Quantidade: ");
            carrinho.merge(id, qtd, Integer::sum);
        }
        if (carrinho.isEmpty()) {
            System.out.println("Carrinho vazio, pedido nao criado.");
            return;
        }

        for (int i = 0; i < estrategiasFrete.size(); i++) {
            System.out.println((i + 1) + " - " + estrategiasFrete.get(i).getNome());
        }
        int escolha = lerInt("Modalidade de frete: ");
        if (escolha < 1 || escolha > estrategiasFrete.size()) {
            System.out.println("Modalidade invalida.");
            return;
        }
        Pedido pedido = servico.criarPedido(cliente, carrinho, estrategiasFrete.get(escolha - 1));
        System.out.println("Pedido criado!");
        System.out.println(Formatador.pedido(pedido));
    }

    private void pagar() {
        int id = lerInt("ID do pedido: ");
        System.out.println("1 - PIX");
        System.out.println("2 - Cartao de credito (token terminado em 0000 = recusado)");
        int forma = lerInt("Forma: ");
        DadosPagamento dados;
        if (forma == 1) {
            dados = DadosPagamento.pix();
        } else if (forma == 2) {
            dados = DadosPagamento.cartao(lerTexto("Token do cartao: "));
        } else {
            System.out.println("Forma invalida.");
            return;
        }
        ResultadoPagamento r = servico.pagar(id, dados);
        System.out.println((r.isAprovado() ? "APROVADO: " : "RECUSADO: ") + r.getMensagem());
    }

    private void listarPedidos() {
        List<Pedido> lista = servico.listarPedidos();
        if (lista.isEmpty()) {
            System.out.println("Nenhum pedido.");
        }
        lista.forEach(p -> System.out.println(Formatador.pedido(p)));
    }

    private int lerInt(String rotulo) {
        while (true) {
            String texto = lerTexto(rotulo);
            try {
                return Integer.parseInt(texto.trim());
            } catch (NumberFormatException e) {
                System.out.println("Digite um numero.");
            }
        }
    }

    private String lerTexto(String rotulo) {
        System.out.print(rotulo);
        if (!entrada.hasNextLine()) {
            System.exit(0);
        }
        return entrada.nextLine();
    }
}
