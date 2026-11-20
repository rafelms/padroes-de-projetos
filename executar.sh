#!/bin/sh
# Compila e executa o sistema de pedidos (requer JDK 17+).
#
#   ./executar.sh                    menu interativo
#   ./executar.sh demo               roteiro automatico com os 4 padroes
#   ./executar.sh testes             testes automatizados (sem JUnit)
#   ./executar.sh exemplo strategy   exemplo isolado de um padrao
#                         adapter | factory | observer
#   ./executar.sh exemplos           roda os 4 exemplos em sequencia
#   ./executar.sh jar                gera dist/sistema-pedidos.jar
cd "$(dirname "$0")" || exit 1
rm -rf build
javac -encoding UTF-8 -d build $(find src test exemplos -name "*.java") || exit 1

exemplo() {
  case "$1" in
    strategy) classe=ExemploStrategy ;;
    adapter)  classe=ExemploAdapter ;;
    factory)  classe=ExemploFactory ;;
    observer) classe=ExemploObserver ;;
    *) echo "Exemplo desconhecido: '$1'. Use: strategy, adapter, factory ou observer."; exit 1 ;;
  esac
  java -cp build "loja.exemplos.$classe"
}

case "$1" in
  demo)     java -cp build loja.Main --demo ;;
  testes)   java -cp build loja.TestesSistema ;;
  exemplo)  exemplo "$2" ;;
  exemplos)
    for nome in strategy adapter factory observer; do
      echo; echo "################ EXEMPLO: $nome ################"; exemplo "$nome" || exit 1
    done ;;
  jar)
    mkdir -p dist
    jar --create --file dist/sistema-pedidos.jar --main-class loja.Main -C build . || exit 1
    echo "Gerado dist/sistema-pedidos.jar"
    echo "  java -jar dist/sistema-pedidos.jar --demo"
    echo "  java -cp dist/sistema-pedidos.jar loja.exemplos.ExemploStrategy" ;;
  "")       java -cp build loja.Main ;;
  *)        echo "Opcao desconhecida: '$1'. Use: demo, testes, exemplo <nome>, exemplos ou jar."; exit 1 ;;
esac
