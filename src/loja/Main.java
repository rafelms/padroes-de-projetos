package loja;

import loja.aplicacao.PedidoService;
import loja.apresentacao.Demonstracao;
import loja.apresentacao.MenuConsole;
import loja.notificacao.NotificadorEmail;
import loja.notificacao.NotificadorSms;
import loja.notificacao.PublicadorEventosPedido;
import loja.notificacao.RegistroAuditoria;
import loja.pagamento.ProcessadorPagamentoFactory;
import loja.repositorio.RepositorioPedidos;
import loja.repositorio.RepositorioProdutos;

/**
 * Ponto de entrada e "raiz de composição": é o ÚNICO lugar que conhece todas
 * as classes concretas e liga umas às outras (injeção de dependência manual,
 * via construtor). Nenhuma outra classe dá "new" em suas dependências.
 *
 * Uso:  java -cp build loja.Main          -> menu interativo
 *       java -cp build loja.Main --demo   -> roteiro automático da apresentação
 */
public class Main {

    public static void main(String[] args) {
        RepositorioProdutos produtos = RepositorioProdutos.comCatalogoInicial();
        RepositorioPedidos pedidos = new RepositorioPedidos();

        ProcessadorPagamentoFactory fabrica =
                new ProcessadorPagamentoFactory("chave-pix-ficticia", "chave-api-ficticia");

        RegistroAuditoria auditoria = new RegistroAuditoria();
        PublicadorEventosPedido publicador = new PublicadorEventosPedido();
        publicador.inscrever(new NotificadorEmail());
        publicador.inscrever(new NotificadorSms());
        publicador.inscrever(auditoria);

        PedidoService servico = new PedidoService(produtos, pedidos, fabrica, publicador);

        if (args.length > 0 && args[0].equals("--demo")) {
            new Demonstracao(servico, auditoria).executar();
        } else {
            new MenuConsole(servico, auditoria).iniciar();
        }
    }
}
