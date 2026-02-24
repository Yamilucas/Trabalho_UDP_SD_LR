package servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import processos.ProcessadorComandos;
import util.GerenciadorLog;

public class ServidorUDP {
    private DatagramSocket socket;
    private ProcessadorComandos processador;
    private GerenciadorLog logger;
    private boolean executando;
    private ConcurrentHashMap<String, String> clientesConectados;
    private static final int PORTA = 1500;
    
    public ServidorUDP() throws IOException {
        this.socket = new DatagramSocket(PORTA);
        this.processador = new ProcessadorComandos();
        this.logger = new GerenciadorLog("log_servidor.txt");
        this.executando = true;
        this.clientesConectados = new ConcurrentHashMap<>();
    }
    
    public void iniciar() {
        logger.registrar("SERVIDOR_INICIADO", "SISTEMA", 
            "Servidor iniciado - IP: " + getIPLocal() + " Porta: " + PORTA);
        
        System.out.println("SERVIDOR UDP DE ARQUIVOS");
        System.out.println("IP: " + getIPLocal());
        System.out.println("Porta: " + PORTA);
        
        while (executando) {
            try {
                byte[] buffer = new byte[65507]; 
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                socket.receive(pacote);
                
                new Thread(() -> processarCliente(pacote)).start();
                
            } catch (IOException e) {
                if (executando) {
                    System.out.println("Erro recebendo pacote: " + e.getMessage());
                }
            }
        }
    }
    
    private void processarCliente(DatagramPacket pacote) {
        String ipCliente = pacote.getAddress().getHostAddress();
        int portaCliente = pacote.getPort();
        String idCliente = ipCliente + ":" + portaCliente;
        
        try {
            String mensagem = new String(pacote.getData(), 0, pacote.getLength()).trim();
            
            if (!clientesConectados.containsKey(idCliente)) {
                clientesConectados.put(idCliente, ipCliente);
                logger.registrar("CLIENTE_CONECTADO", ipCliente, "Porta: " + portaCliente);
                System.out.println("Cliente conectado: " + idCliente);
            }
            
            System.out.println("📨 [" + idCliente + "]: " + 
                (mensagem.length() > 50 ? mensagem.substring(0, 50) + "..." : mensagem));
            
            String resposta = processador.processar(mensagem, ipCliente);
            enviarResposta(ipCliente, portaCliente, resposta);
            
            if (mensagem.startsWith("UPLOAD|") && resposta.startsWith("UPLOAD_OK|")) {
                notificarAtualizacao();
            }
            
        } catch (Exception e) {
            logger.registrar("ERRO_PROCESSAMENTO", ipCliente, e.getMessage());
            try {
                enviarResposta(ipCliente, portaCliente, "ERRO|" + e.getMessage());
            } catch (IOException ex) {
                System.out.println("Erro enviando resposta de erro: " + ex.getMessage());
            }
        }
    }
    
    private void enviarResposta(String ip, int porta, String resposta) throws IOException {
        byte[] dados = resposta.getBytes();
        DatagramPacket pacote = new DatagramPacket(dados, dados.length,
            InetAddress.getByName(ip), porta);
        socket.send(pacote);
        
        System.out.println("📤 [" + ip + ":" + porta + "]: " + 
            (resposta.length() > 50 ? resposta.substring(0, 50) + "..." : resposta));
    }
    
 private void notificarAtualizacao() {
    System.out.println("Notificando " + clientesConectados.size() + " clientes sobre atualização");
    
    for (String idCliente : clientesConectados.keySet()) {
        try {
            String[] partes = idCliente.split(":");
            String ip = partes[0];
            int porta = Integer.parseInt(partes[1]);
            enviarResposta(ip, porta, "ATUALIZAR");
        } catch (Exception e) {
            System.out.println("⚠️ Cliente desconectado: " + idCliente);
            clientesConectados.remove(idCliente);
        }
    }
}
    private String getIPLocal() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
    
    public void parar() {
        executando = false;
        if (socket != null) {
            socket.close();
        }
        logger.registrar("SERVIDOR_PARADO", "SISTEMA", "Servidor finalizado");
        System.out.println("Servidor parado");
    }


    public static void main(String[] args) {
        try {
            ServidorUDP servidor = new ServidorUDP();
            servidor.iniciar();
        } catch (Exception e) {
            System.out.println("Erro ao iniciar servidor: " + e.getMessage());
        }
    }
}
