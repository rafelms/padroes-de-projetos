# Referências

Fontes para aprofundar cada padrão usado (ou deliberadamente **não** usado) neste projeto.

- **Refactoring.Guru**: explicação com diagramas, em português, e exemplos em Java.
- **Java Design Patterns**: catálogo open source com implementações em Java.
- **SourceMaking**: catálogo clássico, com a intenção e a estrutura de cada padrão do GoF.

## Padrões usados no projeto

| Padrão | Onde no código | Refactoring.Guru (pt-BR) | Java Design Patterns | SourceMaking |
|---|---|---|---|---|
| **Strategy** | `src/loja/frete/` | [Strategy](https://refactoring.guru/pt-br/design-patterns/strategy) | [Strategy](https://java-design-patterns.com/patterns/strategy/) | [Strategy](https://sourcemaking.com/design_patterns/strategy) |
| **Adapter** | `src/loja/pagamento/PixAdapter.java`, `CartaoCreditoAdapter.java` | [Adapter](https://refactoring.guru/pt-br/design-patterns/adapter) | [Adapter](https://java-design-patterns.com/patterns/adapter/) | [Adapter](https://sourcemaking.com/design_patterns/adapter) |
| **Factory** (Simple Factory) | `src/loja/pagamento/ProcessadorPagamentoFactory.java` | [Comparação entre fábricas](https://refactoring.guru/pt-br/design-patterns/factory-comparison) · [Factory Method](https://refactoring.guru/pt-br/design-patterns/factory-method) | [Factory](https://java-design-patterns.com/patterns/factory/) | — |
| **Observer** | `src/loja/notificacao/` | [Observer](https://refactoring.guru/pt-br/design-patterns/observer) | [Observer](https://java-design-patterns.com/patterns/observer/) | [Observer](https://sourcemaking.com/design_patterns/observer) |

> **Sobre a Factory:** o projeto usa a *Simple Factory*, que não é um dos 23 padrões do GoF.
> A página "Comparação entre fábricas" do Refactoring.Guru explica a diferença entre
> Simple Factory, Factory Method e Abstract Factory, que é exatamente a discussão da
> seção 5 da [documentação](DOCUMENTACAO.md).

## Padrões discutidos na análise crítica (não usados)

| Padrão | Por que aparece aqui | Referência |
|---|---|---|
| **State** | Alternativa ao `switch` de `StatusPedido` (exercício 6 do guia) | [State](https://refactoring.guru/pt-br/design-patterns/state) |
| **Facade** | Comparação com Adapter ("isso não seria um Facade?") | [Facade](https://refactoring.guru/pt-br/design-patterns/facade) |
| **Singleton** | Por que os repositórios não são Singleton | [Singleton](https://refactoring.guru/pt-br/design-patterns/singleton) |
| Catálogo completo | Os 22 padrões clássicos organizados por categoria | [Catálogo](https://refactoring.guru/pt-br/design-patterns/catalog) |

## Arquitetura e injeção de dependência

- Martin Fowler: [Inversion of Control Containers and the Dependency Injection pattern](https://martinfowler.com/articles/injection.html). É a base da injeção por construtor usada em todo o projeto.
- Mark Seemann: [Composition Root](https://blog.ploeh.dk/2011/07/28/CompositionRoot/). É o papel do `Main.java`, o único lugar que cria as classes concretas.

## Java (documentação oficial da Oracle)

- [Interfaces (The Java Tutorials)](https://docs.oracle.com/javase/tutorial/java/IandI/createinterface.html). Todos os padrões do projeto dependem de programar para interfaces.
- [`java.util.Observer`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Observer.html), que está **depreciado** desde o Java 9. Por isso o projeto define a própria interface `ObservadorPedido`.
- [`java.beans.PropertyChangeSupport`](https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/java/beans/PropertyChangeSupport.html): a alternativa da própria JDK para o Observer.

## Livro de referência

- Gamma, Helm, Johnson e Vlissides (a "Gangue dos Quatro", GoF). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley, 1994. ISBN 978-0201633610. [Página da editora](https://www.informit.com/store/design-patterns-elements-of-reusable-object-oriented-9780201633610). É a origem de Strategy, Adapter, Observer, Factory Method, State, Facade e Singleton. Há edição em português: *Padrões de Projeto: Soluções Reutilizáveis de Software Orientado a Objetos* (Bookman).
