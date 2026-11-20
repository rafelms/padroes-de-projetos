# Roteiro de Apresentação (30 min) — com falas e linhas do código

> Como usar: os blocos **🗣️ Fala** são o que você diz. Os blocos **📂 Mostrar** dizem qual
> arquivo/linha abrir na tela. Os tempos seguem a estrutura obrigatória do trabalho.
>
> Antes de começar: deixe abertos o terminal na pasta `sistema-pedidos`, o arquivo
> `docs/uml.html` no navegador e o editor com `PedidoService.java`.

---

## Bloco 1 — Problema e contexto (5 min)

🗣️ **Fala**

> "Nosso problema é o backend de uma loja online pequena. Parece simples, mas tem quatro
> exigências que puxam o design em direções diferentes.
>
> Primeira: o **frete**. Temos PAC, SEDEX e retirada na loja, cada um com uma fórmula
> própria, e o PAC ainda tem uma regra só dele — é grátis acima de 300 reais. O cliente
> escolhe a modalidade na hora da compra.
>
> Segunda: o **pagamento**. Aceitamos Pix e cartão, só que cada um vem de um fornecedor
> diferente, com uma biblioteca que a gente **não pode alterar**. O SDK do banco trabalha
> com valor em centavos, do tipo `long`. O gateway de cartão é internacional: recebe o valor
> como texto, responde com código HTTP e mensagem de erro em inglês. São duas interfaces
> incompatíveis.
>
> Terceira: o **ciclo de vida** do pedido — aguardando pagamento, pago, enviado, entregue
> ou cancelado — e o sistema não pode permitir, por exemplo, enviar algo que não foi pago.
>
> Quarta: a cada mudança de status, **várias partes precisam ser avisadas**: o cliente por
> e-mail, por SMS se ele tiver telefone, e uma trilha de auditoria. E essa lista tende a
> crescer.
>
> Repare que são três tipos de problema diferentes: um problema de **criação** de objetos,
> um problema de **encaixe** entre interfaces e problemas de **comportamento**. Por isso
> faz sentido usar padrões de três categorias — não fomos procurar lugar para colocar
> padrão; o problema pediu."

📂 **Mostrar**: o README com a tabela de padrões — [README.md](../README.md).

🗣️ **Fala (escopo)**

> "Simplificações assumidas: os dados ficam em memória, e os SDKs de pagamento são
> simulados no pacote `externo`. Mas eles têm regras reais de aprovação e recusa, então o
> sistema funciona de ponta a ponta — não é protótipo de tela."

---

## Bloco 2 — Arquitetura da solução (5 min)

📂 **Mostrar**: a árvore de pastas no editor e depois o diagrama `docs/uml.html`.

🗣️ **Fala**

> "A arquitetura é em camadas. Embaixo está o **domínio** — `Pedido`, `Produto`, `Cliente`,
> `StatusPedido` — que não depende de ninguém. Acima, a camada de **aplicação**, com o
> `PedidoService`, que orquestra os casos de uso. Ao redor, os módulos de frete, pagamento
> e notificação, cada um com seu padrão. E em cima a **apresentação**, que é só um menu de
> console: ela lê a entrada e chama o serviço, sem regra de negócio nenhuma."

📂 **Mostrar**: [Pedido.java:64-70](../src/loja/dominio/Pedido.java#L64)

```java
64    public void alterarStatus(StatusPedido novo) {
65        if (!status.podeMudarPara(novo)) {
66            throw new IllegalStateException(
67                    "Pedido #" + id + ": transicao invalida de " + status + " para " + novo);
68        }
69        this.status = novo;
70    }
```

> "O domínio se protege sozinho: o pedido só muda de status se a transição for válida. A
> regra de quais transições valem está no enum, em
> [StatusPedido.java:17](../src/loja/dominio/StatusPedido.java#L17)."

📂 **Mostrar**: [Main.java:24-43](../src/loja/Main.java#L24)

```java
25        RepositorioProdutos produtos = RepositorioProdutos.comCatalogoInicial();
26        RepositorioPedidos pedidos = new RepositorioPedidos();
28        ProcessadorPagamentoFactory fabrica =
29                new ProcessadorPagamentoFactory("chave-pix-ficticia", "chave-api-ficticia");
31        RegistroAuditoria auditoria = new RegistroAuditoria();
32        PublicadorEventosPedido publicador = new PublicadorEventosPedido();
33        publicador.inscrever(new NotificadorEmail());
34        publicador.inscrever(new NotificadorSms());
35        publicador.inscrever(auditoria);
37        PedidoService servico = new PedidoService(produtos, pedidos, fabrica, publicador);
```

> "O `Main` é a raiz de composição: é o **único lugar** que dá `new` nas classes concretas
> e entrega tudo pronto pelo construtor. O `PedidoService` nunca cria as próprias
> dependências. Isso é o que permite que os testes montem o sistema com outras peças."

📂 **Mostrar**: o diagrama — aponte as quatro cores.

> "No diagrama, cada cor é um padrão: azul é a Factory, verde é o Adapter, amarelo é o
> Strategy, rosa é o Observer. E notem que o `PedidoService`, no centro, só aponta para
> **interfaces**. É isso que o deixa estável."

---

## Bloco 3 — Demonstração (5–10 min)

📂 **Rodar no terminal**:

```bat
executar.bat demo
```

Narre cada seção da saída conforme aparece:

🗣️ **Seção "1) STRATEGY"**

> "Aqui é o **mesmo carrinho** — um mouse e dois cabos — com três estratégias de frete.
> PAC deu R$ 13,75, SEDEX R$ 23,50, retirada R$ 0. Logo abaixo, um carrinho acima de 300
> reais com PAC: frete grátis. Essa regra está só dentro da classe do PAC."

🗣️ **Seção "2) FACTORY + ADAPTER"**

> "Pedido 1 pago por Pix: aprovado, com um código `PIX-...` que veio do SDK do banco.
> Pedido 4 no cartão com um token terminado em 0000: o gateway devolveu 402
> `card_declined` e o nosso sistema mostra 'Cartão recusado pela operadora', em português.
> O status continua aguardando pagamento e o estoque não foi mexido. Depois, com um token
> válido, aprovado com código `ch_...` — formato do outro fornecedor."

🗣️ **Seção "3) OBSERVER"**

> "Ao enviar o pedido da Ana, saem e-mail **e** SMS, porque ela tem telefone. Ao enviar o
> do Bruno, só e-mail — ele não cadastrou telefone. Quem decidiu isso foi o próprio
> observador de SMS; o serviço nem sabe."

🗣️ **Seção "4) REGRAS DE NEGÓCIO"**

> "Tentei cancelar um pedido já entregue: bloqueado pelo domínio. Tentei comprar três
> headsets com só dois em estoque: bloqueado."

🗣️ **Seção "TRILHA DE AUDITORIA" e "ESTOQUE FINAL"**

> "A auditoria é um terceiro observador, que guarda o histórico de todas as transições. E
> no estoque final dá para conferir: só baixou o que foi efetivamente pago."

📂 **Opcional (se sobrar tempo)**: rodar `executar.bat` e fazer um pedido pelo menu
interativo; e rodar `executar.bat testes` mostrando **19 verificações OK, 0 falha(s)**.

---

## Bloco 4 — Explicação dos padrões (10 min)

### 4.1 Strategy — frete (≈2,5 min)

📂 **Mostrar**: [EstrategiaFrete.java](../src/loja/frete/EstrategiaFrete.java) e
[FretePac.java:23-29](../src/loja/frete/FretePac.java#L23)

```java
23    public CotacaoFrete calcular(Pedido pedido) {
24        if (pedido.getSubtotal().compareTo(MINIMO_FRETE_GRATIS) >= 0) {
25            return new CotacaoFrete(BigDecimal.ZERO, 7);
26        }
27        BigDecimal peso = BigDecimal.valueOf(pedido.getPesoTotalKg());
28        return new CotacaoFrete(VALOR_BASE.add(VALOR_POR_KG.multiply(peso)), 7);
29    }
```

🗣️ **Fala**

> "O problema: três algoritmos de frete, escolhidos em tempo de execução. A interface
> `EstrategiaFrete` tem um método só, `calcular`. Cada modalidade é uma classe. Aqui no PAC,
> as linhas 24 e 25 são a regra de frete grátis — ela pertence **só** ao PAC, então fica
> encapsulada aqui."

📂 **Mostrar**: [PedidoService.java:61-62](../src/loja/aplicacao/PedidoService.java#L61)

```java
61        CotacaoFrete cotacao = estrategiaFrete.calcular(pedido);          // STRATEGY
62        pedido.aplicarFrete(estrategiaFrete.getNome(), cotacao.getValor(), cotacao.getPrazoDias());
```

> "No serviço, a linha 61 chama `calcular` sem saber qual estratégia é. Quem escolhe é o
> cliente, lá no menu: [MenuConsole.java:27](../src/loja/apresentacao/MenuConsole.java#L27)
> tem a lista das estratégias e a linha 93 passa a escolhida.
>
> **Alternativa**: um `switch` com as fórmulas dentro do serviço. **Sem o padrão**, todas as
> regras ficariam misturadas num método só, e cada modalidade nova mexeria nele, com risco
> de quebrar as outras. **Com o padrão**, modalidade nova é uma classe nova, e cada regra é
> testada isoladamente — os três primeiros testes fazem exatamente isso."

### 4.2 Adapter — gateways de pagamento (≈2,5 min)

📂 **Mostrar** lado a lado os dois SDKs externos:
[BancoPixApi.java:12](../src/loja/pagamento/externo/BancoPixApi.java#L12) e
[CardGatewayClient.java:22](../src/loja/pagamento/externo/CardGatewayClient.java#L22)

```java
// BancoPixApi.java:12
public CobrancaPix criarCobranca(String chaveRecebedor, long valorEmCentavos, String descricao)
// CardGatewayClient.java:22
public GatewayResponse charge(String cardToken, String amount, String currency)
```

🗣️ **Fala**

> "Esse é o problema: duas APIs que fazem a mesma coisa — cobrar — com assinaturas
> completamente diferentes. Uma recebe centavos como `long` e responde 'CONCLUIDA'. A outra
> recebe o valor como `String`, responde código HTTP 200 ou 402 e erro em inglês. E nós não
> podemos alterar nenhuma das duas."

📂 **Mostrar**: [ProcessadorPagamento.java](../src/loja/pagamento/ProcessadorPagamento.java) e
[PixAdapter.java:25-33](../src/loja/pagamento/PixAdapter.java#L25)

```java
25    public ResultadoPagamento processar(Pedido pedido, DadosPagamento dados) {
26        long centavos = pedido.getTotal().multiply(new BigDecimal("100")).longValue();
27        CobrancaPix cobranca = api.criarCobranca(chavePixDaLoja, centavos, "Pedido #" + pedido.getId());
29        if ("CONCLUIDA".equals(cobranca.getSituacao())) {
30            return ResultadoPagamento.aprovado(cobranca.getTxid());
31        }
32        return ResultadoPagamento.recusado("Cobranca Pix rejeitada pelo banco");
```

> "`ProcessadorPagamento` é a interface que **o nosso sistema** quer — o *Target*. O
> `PixAdapter` implementa essa interface e, por dentro, conversa com o SDK do banco. Linha
> 26: converte reais para centavos. Linha 29: traduz o 'CONCLUIDA' do banco para o nosso
> `ResultadoPagamento`."

📂 **Mostrar**: [CartaoCreditoAdapter.java:22-38](../src/loja/pagamento/CartaoCreditoAdapter.java#L22)

```java
24        GatewayResponse resposta = gateway.charge(dados.getTokenCartao(), valor, "BRL");
26        if (resposta.getHttpStatus() == 200) {
27            return ResultadoPagamento.aprovado(resposta.getTransactionId());
28        }
29        return ResultadoPagamento.recusado(traduzirErro(resposta.getErrorCode()));
...
33        if ("card_declined".equals(codigo)) return "Cartao recusado pela operadora";
34        if ("insufficient_funds".equals(codigo)) return "Limite insuficiente";
```

> "O adapter de cartão faz o mesmo papel: linha 26 traduz o código HTTP, e o método
> `traduzirErro`, a partir da linha 32, converte o erro em inglês para uma mensagem nossa.
>
> **Alternativa**: chamar os SDKs direto no serviço, com `if`. **Sem o padrão**, centavos,
> código HTTP e erro em inglês vazariam para a regra de negócio, e trocar de fornecedor de
> cartão significaria reescrever o `PedidoService`. **Com o padrão**, cada tradução mora num
> único arquivo; se o banco mudar o SDK, só o `PixAdapter` muda.
>
> E por que não Facade? Facade serve para **simplificar** um subsistema complexo. Aqui o
> problema não é complexidade, é **incompatibilidade** — e precisamos que os dois sejam
> intercambiáveis atrás do mesmo contrato. Isso é Adapter."

### 4.3 Factory — criação do processador (≈2 min)

📂 **Mostrar**: [ProcessadorPagamentoFactory.java:26-35](../src/loja/pagamento/ProcessadorPagamentoFactory.java#L26)

```java
26    public ProcessadorPagamento criar(FormaPagamento forma) {
27        switch (forma) {
28            case PIX:
29                return new PixAdapter(new BancoPixApi(), chavePixDaLoja);
30            case CARTAO_CREDITO:
31                return new CartaoCreditoAdapter(new CardGatewayClient(apiKeyGatewayCartao));
32            default:
33                throw new IllegalArgumentException("Forma de pagamento nao suportada: " + forma);
```

🗣️ **Fala**

> "O Adapter resolveu o encaixe, mas sobrou uma pergunta: **quem monta** o adapter certo,
> com o SDK certo e a credencial certa? Isso depende da escolha do cliente em tempo de
> execução. Se ficasse no serviço, o `PedidoService` teria que importar os SDKs e conhecer
> chave Pix e API key.
>
> A fábrica centraliza isso. Linhas 29 e 31: ela sabe que Pix é `PixAdapter` com a chave da
> loja, e cartão é `CartaoCreditoAdapter` com a API key. E devolve só a interface."

📂 **Mostrar**: [PedidoService.java:81-82](../src/loja/aplicacao/PedidoService.java#L81)

```java
81        ProcessadorPagamento processador = fabricaPagamentos.criar(dados.getForma()); // FACTORY
82        ResultadoPagamento resultado = processador.processar(pedido, dados);            // ADAPTER
```

> "Essas duas linhas são onde Factory e Adapter se encontram. A 81 pede à fábrica; a 82 usa
> o processador sem saber qual gateway está por trás.
>
> **Impacto na evolução**: para aceitar boleto, crio o enum, o `BoletoAdapter` e **um
> `case`** aqui. O `PedidoService` não é tocado.
>
> Usei **Simple Factory**, e não Factory Method ou Abstract Factory, de propósito: não existe
> família de produtos relacionados nem hierarquia de criadores. As variantes do GoF só
> adicionariam classes."

### 4.4 Observer — notificações (≈3 min)

📂 **Mostrar**: [PedidoService.java:130-134](../src/loja/aplicacao/PedidoService.java#L130)

```java
130    private void mudarStatus(Pedido pedido, StatusPedido novoStatus) {
131        StatusPedido anterior = pedido.getStatus();
132        pedido.alterarStatus(novoStatus);
133        publicador.notificar(pedido, anterior);                                         // OBSERVER
134    }
```

🗣️ **Fala**

> "Toda mudança de status do sistema passa por este método privado — pagar, enviar,
> entregar e cancelar chamam ele. A linha 133 avisa o publicador. Ou seja, é **impossível**
> mudar status sem notificar."

📂 **Mostrar**: [PublicadorEventosPedido.java:27-35](../src/loja/notificacao/PublicadorEventosPedido.java#L27)

```java
27    public void notificar(Pedido pedido, StatusPedido statusAnterior) {
28        for (ObservadorPedido observador : observadores) {
29            try {
30                observador.statusAlterado(pedido, statusAnterior);
31            } catch (RuntimeException e) {
32                System.out.println("  [AVISO] Falha em " + observador.getClass().getSimpleName()
```

> "O publicador percorre a lista de observadores. Um detalhe importante: o `try/catch` da
> linha 29. Se o servidor de SMS cair, o e-mail e a auditoria continuam, e o pagamento
> **não** é desfeito. Isso está coberto pelo teste
> [TestesSistema.java:102](../test/loja/TestesSistema.java#L102), que injeta um observador
> que lança exceção."

📂 **Mostrar**: [NotificadorSms.java:13-22](../src/loja/notificacao/NotificadorSms.java#L13)

> "Cada observador decide o que lhe interessa: o SMS ignora cliente sem telefone, linha 14,
> e só reage a ENVIADO ou ENTREGUE, linha 18.
>
> **Alternativa**: chamar e-mail, SMS e auditoria diretamente dentro de `pagar`, `enviar`,
> `cancelar`... **Sem o padrão**, essas chamadas se repetiriam em cinco lugares, esquecer uma
> seria um bug silencioso, e adicionar WhatsApp mexeria em todos os métodos. **Com o
> padrão**, nova notificação é uma classe nova e uma linha `inscrever` no `Main`."

---

## Bloco 5 — Análise crítica (5 min)

🗣️ **Fala — onde NÃO usamos padrão**

> "Tão importante quanto usar é saber onde **não** usar.
>
> - O status do pedido é o caso clássico para o padrão **State**. Não usei: são cinco estados,
>   a regra de transição é um `switch` de dez linhas e nenhum estado tem comportamento
>   próprio. State criaria cinco classes para substituir isso
>   ([StatusPedido.java:17-28](../src/loja/dominio/StatusPedido.java#L17)). Se um dia cada
>   estado tiver regras próprias, aí vale migrar.
> - **Builder** para o `Pedido`: o construtor tem três parâmetros e já valida o invariante.
>   Builder seria cerimônia.
> - **Singleton** para os repositórios: seria estado global escondido e atrapalharia os
>   testes, que montam um repositório novo a cada caso.
> - Não criei **interface para o repositório**: só existe a versão em memória — seria
>   abstração especulativa.
> - **Baixa de estoque não é um observador**, mesmo parecendo 'uma reação ao pagamento'. É
>   regra crítica: tem que acontecer junto com o pagamento, não como efeito opcional que
>   pode falhar em silêncio. Está direto no serviço,
>   [PedidoService.java:84-89](../src/loja/aplicacao/PedidoService.java#L84)."

🗣️ **Fala — complexidade e trade-offs**

> "Os padrões têm custo. O frete virou uma interface e três classes em vez de um método. Para
> entender o fluxo de pagamento, a pessoa abre três ou quatro arquivos. Ao ler
> `processador.processar`, não dá para saber qual gateway roda sem olhar a fábrica. E o
> Observer esconde o fluxo: em `mudarStatus` você não vê quem é notificado, tem que ir no
> `Main`.
>
> O trade-off foi: aceitamos mais arquivos e mais indireção em troca de um serviço central
> estável, que recebe nova forma de pagamento, novo frete ou nova notificação **sem ser
> alterado**.
>
> Limitações que eu assumo: o Observer é síncrono — um SMS lento atrasaria a resposta; em
> produção eu colocaria uma fila. A Simple Factory ainda exige editar o `switch` a cada forma
> nova — não é 100% fechada para modificação, mas a alternativa seria registro dinâmico,
> desproporcional para o tamanho do sistema. E o estoque não é transacional: em
> concorrência real precisaria de lock no banco.
>
> Resumindo: cada padrão aqui responde a uma tensão concreta do problema, e onde não havia
> tensão, a gente preferiu código simples. Obrigado."

---

## Perguntas prováveis (cola rápida)

| Pergunta | Resposta curta |
|---|---|
| "Simple Factory é padrão GoF?" | Não é um dos 23 do GoF; é a forma mais simples do padrão Factory. Escolhi ela porque não há hierarquia de criadores — Factory Method seria complexidade sem ganho. Está justificado na documentação. |
| "Por que Strategy e não um enum com método?" | Funcionaria para fórmulas fixas, mas o enum fica fechado para extensão e acopla todas as regras num arquivo. Com interface, uma transportadora nova pode até vir de outro módulo. |
| "O Observer não é só um for?" | O `for` é a implementação; o padrão é a inversão de dependência: o serviço não conhece e-mail nem SMS. Prova: adicionei auditoria sem tocar no serviço. |
| "E se dois clientes pagarem o último item ao mesmo tempo?" | Limitação conhecida: precisaria transação/lock. A revalidação antes de cobrar (linha 75 do `PedidoService`) cobre o caso sequencial. |
| "Os SDKs são reais?" | São simulados, mas com regras de aprovação/recusa reais; a estrutura seria a mesma com um SDK de verdade — só o Adaptee mudaria. |
| "Como provar que funciona?" | `executar.bat testes` → 19 verificações OK, cobrindo cada padrão. |
