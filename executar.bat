@echo off
REM Compila e executa o sistema de pedidos (requer JDK 17+).
REM
REM   executar.bat                    menu interativo
REM   executar.bat demo               roteiro automatico com os 4 padroes
REM   executar.bat testes             testes automatizados (sem JUnit)
REM   executar.bat exemplo strategy   exemplo isolado de um padrao
REM                        adapter ^| factory ^| observer
REM   executar.bat exemplos           roda os 4 exemplos em sequencia
REM   executar.bat jar                gera dist\sistema-pedidos.jar
setlocal
cd /d "%~dp0"
if exist build rmdir /s /q build
dir /s /b src\*.java test\*.java exemplos\*.java > fontes.txt
javac -encoding UTF-8 -d build @fontes.txt
if errorlevel 1 (
    echo Erro de compilacao.
    del fontes.txt
    exit /b 1
)
del fontes.txt

if "%~1"=="" goto menu
if /i "%~1"=="demo" goto demo
if /i "%~1"=="testes" goto testes
if /i "%~1"=="exemplo" goto exemplo
if /i "%~1"=="exemplos" goto exemplos
if /i "%~1"=="jar" goto jar
echo Opcao desconhecida: "%~1". Use: demo, testes, exemplo ^<nome^>, exemplos ou jar.
exit /b 1

:menu
java -cp build loja.Main
exit /b %errorlevel%

:demo
java -cp build loja.Main --demo
exit /b %errorlevel%

:testes
java -cp build loja.TestesSistema
exit /b %errorlevel%

:exemplo
call :rodar_exemplo %2
exit /b %errorlevel%

:exemplos
for %%n in (strategy adapter factory observer) do (
    echo.
    echo ################ EXEMPLO: %%n ################
    call :rodar_exemplo %%n
    if errorlevel 1 exit /b 1
)
exit /b 0

:jar
if not exist dist mkdir dist
jar --create --file dist\sistema-pedidos.jar --main-class loja.Main -C build .
if errorlevel 1 exit /b 1
echo Gerado dist\sistema-pedidos.jar
echo   java -jar dist\sistema-pedidos.jar --demo
echo   java -cp dist\sistema-pedidos.jar loja.exemplos.ExemploStrategy
exit /b 0

:rodar_exemplo
set "classe="
if /i "%~1"=="strategy" set "classe=ExemploStrategy"
if /i "%~1"=="adapter"  set "classe=ExemploAdapter"
if /i "%~1"=="factory"  set "classe=ExemploFactory"
if /i "%~1"=="observer" set "classe=ExemploObserver"
if not defined classe (
    echo Exemplo desconhecido: "%~1". Use: strategy, adapter, factory ou observer.
    exit /b 1
)
java -cp build loja.exemplos.%classe%
exit /b %errorlevel%
