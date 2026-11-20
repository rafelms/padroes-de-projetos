package loja.exemplos;

import java.math.BigDecimal;
import java.util.List;
import loja.dominio.Cliente;
import loja.dominio.ItemPedido;
import loja.dominio.Pedido;
import loja.dominio.Produto;
import loja.frete.CotacaoFrete;
import loja.frete.EstrategiaFrete;
import loja.frete.FretePac;
import loja.frete.FreteSedex;
import loja.frete.RetiradaNaLoja;

/**
 * EXEMPLO ISOLADO: STRATEGY (frete).
 *
 * Mostra duas coisas:
 *   1. O MESMO pedido calculado por estratégias diferentes, sem nenhum if/switch.
 *   2. Como criar uma estratégia NOVA (motoboy) sem alterar nenhuma classe existente.
 *
 * Rodar:  executar.bat exemplo strategy     (ou ./executar.sh exemplo strategy)
 */
public class ExemploStrategy {

    /** Estratégia nova, criada só neste exemplo: R$ 15 fixos, entrega no mesmo dia. */
    static class FreteMotoboy implements EstrategiaFrete {
        @Override
        public String getNome() {
            return "MOTOBOY";
        }

        @Override
        public CotacaoFrete calcular(Pedido pedido) {
            return new CotacaoFrete(new BigDecimal("15.00"), 0);
        }
    }

    public static void main(String[] args) {
        Produto mouse = new Produto(1, "Mouse sem fio", new BigDecimal("89.90"), 0.3, 10);
        Produto cabo = new Produto(2, "Cabo HDMI 2m", new BigDecimal("29.90"), 0.2, 10);
        Cliente cliente = new Cliente("Cliente Exemplo", "cliente@example.com", null);

        Pedido pedido = new Pedido(1, cliente, List.of(
                new ItemPedido(mouse, 1),
                new ItemPedido(cabo, 2)));

        System.out.println("Pedido: subtotal R$ " + pedido.getSubtotal()
                + ", peso " + pedido.getPesoTotalKg() + " kg\n");

        // O código abaixo só conhece a INTERFACE EstrategiaFrete.
        // Trocar a regra de frete = trocar o objeto da lista. Nenhum if.
        List<EstrategiaFrete> estrategias = List.of(
                new FretePac(),
                new FreteSedex(),
                new RetiradaNaLoja(),
                new FreteMotoboy()); // <- estratégia nova, nenhuma classe existente mudou

        for (EstrategiaFrete estrategia : estrategias) {
            CotacaoFrete cotacao = estrategia.calcular(pedido);
            System.out.printf("  %-18s R$ %6s   prazo: %d dia(s)%n",
                    estrategia.getNome(), cotacao.getValor(), cotacao.getPrazoDias());
        }

        System.out.println("\nPara pensar: onde estaria a regra de frete gratis do PAC se");
        System.out.println("tudo isso fosse um switch dentro do PedidoService?");
    }
}
