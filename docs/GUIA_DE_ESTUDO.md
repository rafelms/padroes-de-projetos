# Guia de estudo — como ler este código

Este guia é para quem fez fork do repositório e quer **entender** os padrões, não só ver
que eles existem. A ideia é: ler pouco, rodar muito, e depois mexer.

> A explicação conceitual completa (UML, justificativas e análise crítica) está em
> [DOCUMENTACAO.md](DOCUMENTACAO.md). Este guia é o mapa prático do código.

---

## 1. Antes de começar (5 min)

```bash
javac -version            # precisa ser 17 ou maior
./executar.sh testes      # Windows: executar.bat testes
```

Se aparecer `Resultado: 19 verificacoes OK, 0 falha(s)`, está tudo pronto.

---

## 2. Ordem de leitura recomendada

Leia nesta ordem. Cada passo depende só dos anteriores.

| # | Arquivo | O que observar |
|---|---------|----------------|
| 1 | `src/loja/dominio/Produto.java`, `Cliente.java`, `ItemPedido.java` | Classes simples. `ItemPedido` guarda o preço do momento da compra. |
| 2 | `src/loja/dominio/Pedido.java` | O pedido calcula subtotal, peso e total, e **valida a própria transição de status**. |
| 3 | `src/loja/dominio/StatusPedido.java` | O ciclo de vida inteiro cabe num `switch`. Leia o comentário: é a decisão de **não** usar State. |
| 4 | `src/loja/frete/` | **Strategy.** Comece pela interface `EstrategiaFrete` e depois veja as três implementações. |
| 5 | `src/loja/pagamento/externo/` | Os "SDKs de terceiros". Repare como os dois são diferentes entre si. **Finja que você não pode alterar estes arquivos.** |
| 6 | `src/loja/pagamento/ProcessadorPagamento.java` → `PixAdapter` → `CartaoCreditoAdapter` | **Adapter.** O contrato que *nós* queremos e as duas traduções. |
| 7 | `src/loja/pagamento/ProcessadorPagamentoFactory.java` | **Factory.** Um único lugar sabe qual SDK e qual credencial usar. |
| 8 | `src/loja/notificacao/` | **Observer.** `ObservadorPedido` (interface), `PublicadorEventosPedido` (quem avisa) e os três observadores. |
| 9 | `src/loja/aplicacao/PedidoService.java` | Onde tudo se encontra. Procure os comentários `// STRATEGY`, `// FACTORY`, `// ADAPTER`, `// OBSERVER`. |
| 10 | `src/loja/Main.java` | A **raiz de composição**: o único lugar que dá `new` nas classes concretas. |
| 11 | `test/loja/TestesSistema.java` | Cada teste monta o sistema do zero. É a melhor documentação do comportamento esperado. |

`src/loja/apresentacao/` (menu, demonstração, formatação) e `src/loja/repositorio/`
(listas em memória) não têm padrões. Leia por último, se quiser.

---

## 3. Cada padrão: onde está, como ver rodando, o que perceber

### Strategy — frete

- **Onde:** `frete/EstrategiaFrete.java` + `FretePac`, `FreteSedex`, `RetiradaNaLoja`.
- **Chamada:** `PedidoService.criarPedido(...)` → `estrategiaFrete.calcular(pedido)`.
- **Rodar:** `./executar.sh exemplo strategy`
- **Perceba:** o exemplo cria uma estratégia nova (`FreteMotoboy`) **dentro do próprio
  arquivo do exemplo**, sem alterar nenhuma classe do sistema. A regra de frete grátis
  acima de R$ 300 existe só dentro de `FretePac`.
- **Pergunta para testar seu entendimento:** o que mudaria no `PedidoService` se o frete
  fosse um `switch`? Quantos arquivos você teria que abrir para criar uma modalidade nova?

### Adapter — gateways de pagamento

- **Onde:** `pagamento/ProcessadorPagamento.java` (Target), `PixAdapter` e
  `CartaoCreditoAdapter` (Adapters), `pagamento/externo/*` (Adaptees).
- **Rodar:** `./executar.sh exemplo adapter`
- **Perceba:** a parte 1 do exemplo chama os SDKs crus e mostra respostas em formatos
  diferentes (`"CONCLUIDA"` x `http=402, error=card_declined`). Na parte 2, os mesmos SDKs
  passam pelos adapters e as respostas ficam no mesmo formato (`ResultadoPagamento`).
- **Traduções feitas pelos adapters:**
  - reais (`BigDecimal`) → centavos (`long`) no Pix;
  - reais (`BigDecimal`) → texto `"134.90"` + moeda `"BRL"` no cartão;
  - códigos em inglês (`card_declined`, `insufficient_funds`...) → mensagens em português.
- **Adapter x Facade:** Facade *simplifica* um subsistema complexo. Aqui o problema é
  *incompatibilidade* entre interfaces que precisam ser trocáveis. Isso é Adapter.

### Factory — criação do processador

- **Onde:** `pagamento/ProcessadorPagamentoFactory.java`.
- **Chamada:** `PedidoService.pagar(...)` → `fabricaPagamentos.criar(dados.getForma())`.
- **Rodar:** `./executar.sh exemplo factory`
- **Perceba:** quem chama a fábrica recebe só a interface. O exemplo também monta uma
  fábrica **sem API key** e mostra que o erro chega traduzido ("Falha de configuracao do
  gateway"), sem quebrar o Pix.
- **Sobre as credenciais:** os valores `chave-pix-ficticia` e `chave-api-ficticia` são
  de mentira. Num sistema real eles viriam de variável de ambiente ou de um cofre de
  segredos (`System.getenv(...)`), **nunca** escritos no código.
- **É GoF?** Simple Factory não é um dos 23 padrões do livro. Foi escolhida de propósito:
  não há família de produtos (Abstract Factory) nem hierarquia de criadores (Factory Method).

### Observer — notificações

- **Onde:** `notificacao/ObservadorPedido.java` (interface), `PublicadorEventosPedido`
  (Subject), `NotificadorEmail`, `NotificadorSms`, `RegistroAuditoria` (observadores).
- **Chamada:** todo status passa por `PedidoService.mudarStatus(...)`, que chama
  `publicador.notificar(...)`. Não existe outro caminho para mudar o status.
- **Rodar:** `./executar.sh exemplo observer`
- **Perceba:** o exemplo inscreve um observador que **sempre lança exceção** e um painel de
  faturamento novo. O painel continua sendo avisado e o pedido continua sendo pago, por
  causa do `try/catch` em `PublicadorEventosPedido.notificar`.
- **"Não é só um for?"** O `for` é a implementação. O padrão é a inversão de dependência:
  o serviço não conhece e-mail, SMS ou painel.

---

## 4. Como o sistema é montado (injeção de dependência manual)

Nenhuma classe cria as próprias dependências. Tudo é passado pelo construtor, e quem
monta é o `Main`:

```java
ProcessadorPagamentoFactory fabrica =
        new ProcessadorPagamentoFactory("chave-pix-ficticia", "chave-api-ficticia");

PublicadorEventosPedido publicador = new PublicadorEventosPedido();
publicador.inscrever(new NotificadorEmail());
publicador.inscrever(new NotificadorSms());
publicador.inscrever(auditoria);

PedidoService servico = new PedidoService(produtos, pedidos, fabrica, publicador);
```

Por isso os testes e os exemplos conseguem montar o **mesmo** `PedidoService` com peças
diferentes (outros observadores, fábrica sem chave etc.) sem nenhum framework.

---

## 5. Exercícios (do mais fácil ao mais difícil)

Faça cada um numa branch do seu fork. Depois de cada um, rode `./executar.sh testes`.

1. **Nova modalidade de frete.** Crie `FreteTransportadora` (R$ 30 fixos + R$ 1,50/kg,
   5 dias) e adicione ao menu em `MenuConsole`. *Critério:* `PedidoService` não muda.
2. **Novo observador.** Crie `NotificadorWhatsApp` que só avisa quando o pedido é
   `ENTREGUE`. Inscreva no `Main`. *Critério:* `PedidoService` não muda.
3. **Nova forma de pagamento.** Simule um SDK `BoletoApi` em `pagamento/externo` (com uma
   API bem diferente das outras), crie `BoletoAdapter`, adicione `BOLETO` ao enum e um
   `case` na fábrica. *Critério:* `PedidoService` não muda.
4. **Credenciais fora do código.** Faça o `Main` ler as chaves com `System.getenv` e usar
   os valores fictícios só se a variável não existir.
5. **Escreva testes** para os exercícios 1 a 3 em `TestesSistema`, no mesmo estilo dos
   existentes (um método por comportamento + chamada em `main`).
6. **Desafio: refatore para State.** Troque o `switch` de `StatusPedido` pelo padrão State
   (uma classe por estado). Depois compare: quantas classes surgiram? O código ficou mais
   claro ou só maior? Escreva sua conclusão. (Spoiler da análise crítica: com 5 estados
   e nenhum comportamento diferente por estado, o `switch` ganha.)
7. **Desafio: Decorator.** Crie um `ProcessadorPagamento` que *envolve* outro e registra
   o tempo de cada cobrança, sem mudar os adapters existentes.

---

## 6. Perguntas para revisar

- Qual classe é a única que conhece todas as classes concretas? Por quê?
- O que acontece com o estoque se o cartão for recusado? Onde isso está garantido?
- Por que a baixa de estoque **não** foi feita como um observador?
- Se o SMS lançar exceção, o pedido é pago? Qual linha garante isso?
- Por que os repositórios não são Singleton?

As respostas estão no código e em [DOCUMENTACAO.md](DOCUMENTACAO.md), seção 5.
