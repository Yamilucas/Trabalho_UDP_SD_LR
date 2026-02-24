package cliente;

import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;

public class ClienteUDP {
    private String serverIP;
    private int serverPort;
    private DatagramSocket socket;
    private static final int TIMEOUT = 100000; 
    
    public ClienteUDP(String serverIP, int serverPort) throws Exception {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.socket = new DatagramSocket(); 
        this.socket.setSoTimeout(TIMEOUT);
    }
    
    public String enviarComando(String comando) {
        try {
            byte[] dados = comando.getBytes();
            DatagramPacket pacote = new DatagramPacket(
                dados, dados.length,
                InetAddress.getByName(serverIP), serverPort
            );
            socket.send(pacote);
            
        
            byte[] buffer = new byte[65507];
            DatagramPacket resposta = new DatagramPacket(buffer, buffer.length);
            socket.receive(resposta);
            
            return new String(resposta.getData(), 0, resposta.getLength()).trim();
            
        } catch (Exception e) {
            return "ERRO|" + e.getMessage();
        }
    }
    
    public String listarArquivos() {
        return enviarComando("LISTAR");
    }
    
public String fazerUpload(File arquivo) {
    try {
        if (!arquivo.exists()) {
            return "ERRO|Arquivo não existe";
        }
        byte[] dados = Files.readAllBytes(arquivo.toPath());
        String dadosBase64 = java.util.Base64.getEncoder().encodeToString(dados);
        String comando = "UPLOAD|" + arquivo.getName() + "|" + arquivo.length() + "|" + dadosBase64;
        return enviarComando(comando);
        
    } catch (Exception e) {
        return "ERRO|" + e.getMessage();
    }
}
    
    public String fazerDownload(String nomeArquivo) {
    String resposta = enviarComando("DOWNLOAD|" + nomeArquivo);
    
    if (resposta.startsWith("ARQUIVO|")) {
        try {
            String[] partes = resposta.split("\\|", 3);
            if (partes.length < 3) {
                return "ERRO|Formato de resposta inválido do servidor";
            }
            
            String nome = partes[1];
            String dadosBase64 = partes[2];
            
            byte[] dados = java.util.Base64.getDecoder().decode(dadosBase64);
            
            File pastaDownloads = new File("downloads_cliente");
            if (!pastaDownloads.exists()) {
                pastaDownloads.mkdirs();
            }
            
            FileOutputStream fos = new FileOutputStream("downloads_cliente/" + nome);
            fos.write(dados);
            fos.close();
            
            return "OK|Download concluído: downloads_cliente/" + nome;
            
        } catch (IllegalArgumentException e) {
            return "ERRO|Dados Base64 inválidos recebidos do servidor";
        } catch (Exception e) {
            return "ERRO|Falha ao salvar: " + e.getMessage();
        }
    }
    return resposta;
}
    
    public String deletarArquivo(String nomeArquivo) {
        return enviarComando("DELETE|" + nomeArquivo);
    }
    
    public void fechar() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}