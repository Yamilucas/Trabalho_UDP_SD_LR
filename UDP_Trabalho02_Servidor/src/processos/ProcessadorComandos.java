package processos;

import controller.Arquivos_Controller;
import java.util.Base64;
import java.util.List;

public class ProcessadorComandos {
    private Arquivos_Controller controller;
    
    public ProcessadorComandos() {
        this.controller = new Arquivos_Controller();
    }
    
    public String processar(String comando, String ipCliente) {
        try {
            if (comando.equals("LISTAR")) {
                return processarListar();
            } 
            else if (comando.startsWith("DOWNLOAD|")) {
                String nomeArquivo = comando.substring(9);
                return processarDownload(nomeArquivo);
            }
            else if (comando.startsWith("DELETE|")) {
                String nomeArquivo = comando.substring(7);
                return processarDelete(nomeArquivo);
            }
            else if (comando.startsWith("UPLOAD|")) {
                return processarUpload(comando);
            }
            else {
                return "ERRO|Comando desconhecido: " + comando;
            }
        } catch (Exception e) {
            return "ERRO|" + e.getMessage();
        }
    }
    
private String processarListar() {
    List<String> arquivos = controller.listarArquivos();
    if (arquivos.isEmpty()) {
        return "LISTA|VAZIA";
    }
    
    StringBuilder resposta = new StringBuilder("LISTA");
    for (String arquivo : arquivos) {
        String nomeArquivo = arquivo.split("\\|")[0];
        resposta.append("|").append(nomeArquivo);
    }
    return resposta.toString();
}
    
    private String processarDownload(String nomeArquivo) {
        byte[] dados = controller.buscarArquivo(nomeArquivo);
        if (dados != null) {
            String dadosBase64 = Base64.getEncoder().encodeToString(dados);
            return "ARQUIVO|" + nomeArquivo + "|" + dadosBase64;
        }
        return "ERRO|Arquivo não encontrado";
    }
    
    private String processarDelete(String nomeArquivo) {
        boolean sucesso = controller.deletarArquivo(nomeArquivo);
        return sucesso ? "DELETE_OK" : "ERRO|Falha ao deletar";
    }
    
    private String processarUpload(String comando) {
        String[] partes = comando.split("\\|", 4);
        if (partes.length < 4) {
            return "ERRO|Formato inválido. Esperado: UPLOAD|nome|tamanho|dadosBase64";
        }
        
        try {
            String nomeArquivo = partes[1];
            long tamanho = Long.parseLong(partes[2]);
            String dadosBase64 = partes[3];
                        byte[] dados = Base64.getDecoder().decode(dadosBase64);
            boolean sucesso = controller.salvarArquivo(nomeArquivo, tamanho, dados);
            return sucesso ? "UPLOAD_OK" : "ERRO|Falha no upload";
            
        } catch (IllegalArgumentException e) {
            return "ERRO|Dados Base64 inválidos: " + e.getMessage();
        } catch (Exception e) {
            return "ERRO|" + e.getMessage();
        }
    }
}