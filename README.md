# Sistema de Pedidos — Padrões de Projeto Integrados

Backend em **Java 17 puro** (sem frameworks, sem dependências) de um e-commerce básico:
catálogo, carrinho, cálculo de frete, pagamento por gateways externos e notificações do
ciclo de vida do pedido.

| Categoria      | Padrão                     | Onde                                   | Problema real que resolve |
|----------------|----------------------------|----------------------------------------|---------------------------|
| Criacional     | **Factory** (Simple Factory) | `pagamento/ProcessadorPagamentoFactory` | Montar o processador certo (SDK + credencial) sem o serviço conhecer classes concretas |
| Estrutural     | **Adapter**                | `pagamento/PixAdapter`, `CartaoCreditoAdapter` | Dois gateways com APIs incompatíveis que não podemos alterar |
| Comportamental | **Strategy**               | `frete/EstrategiaFrete` + 3 implementações | Regras de frete diferentes, escolhidas pelo cliente em tempo de execução |
| Comportamental | **Observer**               | `notificacao/PublicadorEventosPedido` + 3 observadores | Várias reações independentes a cada mudança de status |

## Como executar

Pré-requisito: **JDK 17+** (`javac -version`). Nenhuma dependência, Maven ou Gradle.

| Comando (Windows) | Comando (Linux/macOS/Git Bash) | O que faz |
|---|---|---|
| `executar.bat demo` | `./executar.sh demo` | Roteiro automático que mostra os 4 padrões em ação |
| `executar.bat testes` | `./executar.sh testes` | 19 verificações automatizadas (sem JUnit) |
| `executar.bat` | `./executar.sh` | Menu interativo: criar, pagar, enviar e cancelar pedidos |
| `executar.bat exemplo strategy` | `./executar.sh exemplo strategy` | Exemplo isolado de um padrão (`strategy`, `adapter`, `factory` ou `observer`) |
| `executar.bat exemplos` | `./executar.sh exemplos` | Roda os 4 exemplos em sequência |
| `executar.bat jar` | `./executar.sh jar` | Gera `dist/sistema-pedidos.jar` executável |

Depois de gerar o JAR:

```bash
java -jar dist/sistema-pedidos.jar --demo
java -cp dist/sistema-pedidos.jar loja.exemplos.ExemploObserver
```

Dica do menu: no cartão, qualquer token **terminado em `0000`** é recusado pelo gateway
simulado, e compras acima de R$ 5.000 dão "limite insuficiente".

### Exemplos por padrão

Cada exemplo em `exemplos/loja/exemplos/` é um `main` curto e comentado que isola **um**
padrão e mostra uma extensão sem alterar o sistema:

| Exemplo | O que demonstra |
|---|---|
| `ExemploStrategy` | Mesmo pedido em 4 estratégias de frete, incluindo uma nova (`FreteMotoboy`) criada no próprio exemplo |
| `ExemploAdapter` | Os SDKs "de terceiros" chamados crus e depois atrás dos adapters, com o mesmo formato de resposta |
| `ExemploFactory` | A fábrica entregando só a interface, e o efeito de uma fábrica sem API key |
| `ExemploObserver` | Um observador novo (painel de faturamento) e um observador que falha sem derrubar os outros |

> Todos os dados do projeto são **fictícios**: clientes, e-mails (`@example.com`), telefones,
> tokens de cartão e credenciais (`chave-pix-ficticia`, `chave-api-ficticia`). Os gateways de
> pagamento são simulados e nenhuma chamada externa é feita.

## Estrutura

```
src/loja/
├── Main.java                  raiz de composição (liga tudo)
├── dominio/                   Produto, Cliente, ItemPedido, Pedido, StatusPedido
├── aplicacao/                 PedidoService (casos de uso)
├── frete/                     STRATEGY
├── pagamento/                 ADAPTER + FACTORY
│   └── externo/               SDKs de terceiros simulados (não podemos alterar)
├── notificacao/               OBSERVER
├── repositorio/               armazenamento em memória
└── apresentacao/              menu de console, demonstração, formatação
test/loja/TestesSistema.java   testes sem framework
exemplos/loja/exemplos/        um exemplo executável por padrão
docs/                          documentação e guia de estudo
```

## Documentos

- [docs/GUIA_DE_ESTUDO.md](docs/GUIA_DE_ESTUDO.md) — **comece por aqui**: ordem de leitura do código, onde está cada padrão e exercícios
- [docs/REFERENCIAS.md](docs/REFERENCIAS.md) — links para a documentação de cada padrão (Refactoring.Guru, Java Design Patterns, Oracle, GoF)
- [docs/DOCUMENTACAO.md](docs/DOCUMENTACAO.md) — problema, arquitetura, UML, justificativa de cada padrão e análise crítica
- [docs/ROTEIRO_APRESENTACAO.md](docs/ROTEIRO_APRESENTACAO.md) — roteiro de 30 min com as falas e as linhas do código
- [docs/uml.html](docs/uml.html) — diagrama de classes renderizado (abra no navegador)
- [docs/diagrama-classes.puml](docs/diagrama-classes.puml) — mesmo diagrama em PlantUML

## Para estudar a partir de um fork

1. Faça o fork e clone o seu repositório.
2. Rode `executar.bat testes` (ou `./executar.sh testes`) para confirmar que compila.
3. Siga o [guia de estudo](docs/GUIA_DE_ESTUDO.md): ele indica a ordem de leitura e traz
   exercícios (novo frete, novo observador, boleto, refatorar para State...).
4. Depois de cada mudança, rode os testes de novo.

No IntelliJ: *File → Open* na pasta do projeto. O módulo já vem configurado com `src`,
`test` e `exemplos` como pastas de código (JDK 17).
