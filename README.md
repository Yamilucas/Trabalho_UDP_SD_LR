Projeto desenvolvido para a disciplina de **Sistemas Distribuídos**, implementado utilizando **Sockets UDP** para comunicação entre cliente e servidor, em um ambiente distribuído.

## Autores

- **Lucas Eufrásio Guimarães**
- **Rodrian**

## Descrição do Projeto
O sistema consiste em uma aplicação distribuída de **Mini Servidor de Arquivos utilizando Sockets UDP**, estruturada em duas partes distintas:

### Cliente
Aplicação com interface gráfica desenvolvida em **Java**, utilizando **JFrame (Swing)**, responsável pela interação com o usuário e pelas funcionalidades de **upload**, **download** e visualização da lista de arquivos disponíveis, além de exibir **barra de progresso** e mensagens de status nos logs da aplicação.

### Servidor
Responsável pelo envio e recebimento de arquivos via **Sockets UDP**, gerenciando os diretórios de **upload** e **download**, realizando a **fragmentação e remontagem de pacotes UDP**, mantendo o registro dos arquivos em uma **tabela SQL** integrada ao sistema, enviando notificações aos clientes conectados e gerando **logs das operações**, como uploads e downloads.

### Estrutura de Execução
- Um computador executando o **cliente (interface gráfica)**
- Outro computador executando o **servidor (armazenamento de arquivos, banco de dados, logs e comunicação UDP)**

## Demonstração do Sistema
Vídeo demonstrando o sistema em funcionamento em um único computador, uma vez que não foi possível realizar a gravação em dois computadores distintos:

- https://www.youtube.com/watch?v=iwTc__IMh6I

