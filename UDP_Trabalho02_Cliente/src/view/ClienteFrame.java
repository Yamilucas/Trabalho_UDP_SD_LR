package view;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import cliente.ClienteUDP;

public class ClienteFrame extends JFrame {

    private ClienteUDP cliente;
    private JTextArea areaLog;
    private JList<String> listaArquivos;
    private DefaultListModel<String> modeloLista;
    private JTextField fieldIP, fieldPorta;
    private JButton btnConectar, btnUpload, btnDownload, btnDeletar, btnAtualizar;
    private JProgressBar progressBarDownload, progressBarUpload;
    private JLabel labelDownload, labelUpload;
    private boolean conectado = false;

    public ClienteFrame() {
        super("Cliente de Arquivos UDP");
        setupInterface();
    }

    private void setupInterface() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);

        JPanel painelConexao = new JPanel(new FlowLayout());
        painelConexao.setBorder(BorderFactory.createTitledBorder("🔗 Conexão"));

        fieldIP = new JTextField("127.0.0.1", 15);
        fieldPorta = new JTextField("1500", 5);
        btnConectar = new JButton("Conectar");

        painelConexao.add(new JLabel("IP:"));
        painelConexao.add(fieldIP);
        painelConexao.add(new JLabel("Porta:"));
        painelConexao.add(fieldPorta);
        painelConexao.add(btnConectar);

        JPanel painelArquivos = new JPanel(new BorderLayout());
        painelArquivos.setBorder(BorderFactory.createTitledBorder("📁 Arquivos no Servidor"));
        painelArquivos.setPreferredSize(new Dimension(350, 400));

        modeloLista = new DefaultListModel<>();
        listaArquivos = new JList<>(modeloLista);
        painelArquivos.add(new JScrollPane(listaArquivos), BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new GridLayout(2, 2, 5, 5));
        btnUpload = new JButton("📤 Upload");
        btnDownload = new JButton("📥 Download");
        btnDeletar = new JButton("🗑 Deletar");
        btnAtualizar = new JButton("🔄 Atualizar");

        // Definir tamanhos preferidos para os botões
        Dimension tamanhoBotao = new Dimension(120, 35);
        btnUpload.setPreferredSize(tamanhoBotao);
        btnDownload.setPreferredSize(tamanhoBotao);
        btnDeletar.setPreferredSize(tamanhoBotao);
        btnAtualizar.setPreferredSize(tamanhoBotao);

        habilitarBotoes(false);

        painelBotoes.add(btnUpload);
        painelBotoes.add(btnDownload);
        painelBotoes.add(btnDeletar);
        painelBotoes.add(btnAtualizar);
        
        JPanel painelBotoesWrapper = new JPanel(new FlowLayout());
        painelBotoesWrapper.add(painelBotoes);
        painelArquivos.add(painelBotoesWrapper, BorderLayout.SOUTH);

        JPanel painelProgressoDownload = new JPanel(new GridLayout(2, 1, 5, 5));
        painelProgressoDownload.setBorder(BorderFactory.createTitledBorder("📥 Progresso do Download"));
        painelProgressoDownload.setPreferredSize(new Dimension(400, 80));
        
        labelDownload = new JLabel("Download: 0%");
        progressBarDownload = new JProgressBar(0, 100);
        progressBarDownload.setStringPainted(true);
        progressBarDownload.setString("0%");
        progressBarDownload.setPreferredSize(new Dimension(350, 20));
        progressBarDownload.setForeground(new Color(0, 100, 200)); // Azul
        
        painelProgressoDownload.add(labelDownload);
        painelProgressoDownload.add(progressBarDownload);

        // Painel de Progresso para Upload
        JPanel painelProgressoUpload = new JPanel(new GridLayout(2, 1, 5, 5));
        painelProgressoUpload.setBorder(BorderFactory.createTitledBorder("📤 Progresso do Upload"));
        painelProgressoUpload.setPreferredSize(new Dimension(400, 80));
        
        labelUpload = new JLabel("Upload: 0%");
        progressBarUpload = new JProgressBar(0, 100);
        progressBarUpload.setStringPainted(true);
        progressBarUpload.setString("0%");
        progressBarUpload.setPreferredSize(new Dimension(350, 20));
        progressBarUpload.setForeground(new Color(50, 150, 50)); // Verde
        
        painelProgressoUpload.add(labelUpload);
        painelProgressoUpload.add(progressBarUpload);

        // Painel unificado de progresso
        JPanel painelProgressoUnificado = new JPanel(new GridLayout(2, 1, 10, 10));
        painelProgressoUnificado.add(painelProgressoDownload);
        painelProgressoUnificado.add(painelProgressoUpload);

        // Área de Log
        areaLog = new JTextArea(8, 60);
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder("📋 Log"));
        scrollLog.setPreferredSize(new Dimension(800, 150));

      
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.add(painelConexao, BorderLayout.NORTH);
        
        JPanel painelCentral = new JPanel(new BorderLayout(15, 15));
        painelCentral.add(painelArquivos, BorderLayout.WEST);
        painelCentral.add(painelProgressoUnificado, BorderLayout.CENTER);
        
        painelPrincipal.add(painelCentral, BorderLayout.CENTER);
        painelPrincipal.add(scrollLog, BorderLayout.SOUTH);
        
        add(painelPrincipal);

        // Listeners dos Botões
        btnConectar.addActionListener(e -> conectarServidor());
        btnUpload.addActionListener(e -> fazerUpload());
        btnDownload.addActionListener(e -> fazerDownload());
        btnDeletar.addActionListener(e -> deletarArquivo());
        btnAtualizar.addActionListener(e -> atualizarLista());
        
        
        limparBarrasProgresso();
    }

    private void conectarServidor() {
        if (conectado) {
            desconectarServidor();
            return;
        }

        new Thread(() -> {
            try {
                atualizarProgressoUpload(0, "Conectando...");

                cliente = new ClienteUDP(fieldIP.getText(), Integer.parseInt(fieldPorta.getText()));

                String resposta = cliente.listarArquivos();

                if (resposta.startsWith("LISTA") || resposta.startsWith("VAZIA")) {
                    conectado = true;
                    habilitarBotoes(true);
                    btnConectar.setText("Desconectar");
                    fieldIP.setEnabled(false);
                    fieldPorta.setEnabled(false);
                    
                    atualizarLista();
                    atualizarProgressoUpload(100, "Conectado");
                    log("Conectado ao servidor");
                } else {
                    atualizarProgressoUpload(0, "Falha na conexão");
                    log("Falha: " + resposta);
                    JOptionPane.showMessageDialog(this, "Falha na conexão: " + resposta, "Erro", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception e) {
                atualizarProgressoUpload(0, "Erro na conexão");
                log("Erro na conexão: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Erro na conexão: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }

    private void desconectarServidor() {
        conectado = false;
        habilitarBotoes(false);
        btnConectar.setText("Conectar");
        fieldIP.setEnabled(true);
        fieldPorta.setEnabled(true);
        modeloLista.clear();
        limparBarrasProgresso();
        
        if (cliente != null) {
            cliente.fechar();
        }
        
        log("Desconectado do servidor");
    }

    private void atualizarLista() {
        if (!conectado || cliente == null) {
            log("Não conectado ao servidor");
            return;
        }

        new Thread(() -> {
            try {
                String resposta = cliente.listarArquivos();

                SwingUtilities.invokeLater(() -> {
                    modeloLista.clear();

                    if (resposta.startsWith("LISTA|")) {
                        String[] partes = resposta.split("\\|");
                        for (int i = 1; i < partes.length; i++) {
                            String arquivoInfo = partes[i].trim();
                            
                            if (!arquivoInfo.isEmpty() && !arquivoInfo.equals("VAZIA")) {
                                if (arquivoInfo.contains("|")) {
                                    String[] infoPartes = arquivoInfo.split("\\|");
                                    if (infoPartes.length >= 1) {
                                        String nomeArquivo = infoPartes[0].trim();
                                        if (!nomeArquivo.isEmpty()) {
                                            modeloLista.addElement(nomeArquivo);
                                        }
                                    }
                                } else {
                                    modeloLista.addElement(arquivoInfo);
                                }
                            }
                        }
                        log("Lista atualizada - " + (modeloLista.size()) + " arquivos");
                    } else if (resposta.equals("VAZIA")) {
                        log("Servidor vazio - nenhum arquivo disponível");
                    } else {
                        log("Resposta inesperada: " + resposta);
                    }
                });

            } catch (Exception e) {
                log("Erro ao atualizar lista: " + e.getMessage());
            }
        }).start();
    }

    private void fazerUpload() {
        if (!conectado || cliente == null) {
            log("⚠ Não conectado ao servidor");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Escolha o arquivo para upload");

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        File arquivo = chooser.getSelectedFile();

        new Thread(() -> {
            try {
                log("⬆ Enviando: " + arquivo.getName());
                limparBarrasProgressoUpload();
                atualizarProgressoUpload(0, "Iniciando upload...");

                simularUploadComProgresso(arquivo);
                
            } catch (Exception e) {
                atualizarProgressoUpload(0, "Erro no upload");
                log("Erro no upload: " + e.getMessage());
            }
        }).start();
    }

    private void simularUploadComProgresso(File arquivo) {
        new Thread(() -> {
            try {
                for (int progresso = 0; progresso <= 100; progresso += 10) {
                    Thread.sleep(200);
                    atualizarProgressoUpload(progresso, 
                        String.format("Enviando: %d%%", progresso));
                }

                String resposta = cliente.fazerUpload(arquivo);

                SwingUtilities.invokeLater(() -> {
                    if (resposta.startsWith("UPLOAD_OK")) {
                        atualizarProgressoUpload(100, "Upload concluído");
                        log("Upload realizado com sucesso");
                        atualizarLista();
                    } else {
                        atualizarProgressoUpload(0, "Erro no upload");
                        log("Erro no upload: " + resposta);
                    }
                });
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("Upload interrompido");
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    atualizarProgressoUpload(0, "Erro no upload");
                    log("Erro no upload: " + e.getMessage());
                });
            }
        }).start();
    }

    private void fazerDownload() {
        if (!conectado || cliente == null) {
            log("Não conectado ao servidor");
            return;
        }

        String nome = listaArquivos.getSelectedValue();

        if (nome == null) {
            log("Selecione um arquivo para download");
            JOptionPane.showMessageDialog(this, "Selecione um arquivo da lista", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                log("⬇ Baixando: " + nome);
                limparBarrasProgressoDownload();
                atualizarProgressoDownload(0, "Iniciando download...");

                simularDownloadComProgresso(nome);
                
            } catch (Exception ex) {
                atualizarProgressoDownload(0, "Erro no download");
                log("Erro no download: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro no Download", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }

    private void simularDownloadComProgresso(String nomeArquivo) {
        new Thread(() -> {
            try {
                for (int progresso = 0; progresso <= 100; progresso += 10) {
                    Thread.sleep(200);
                    atualizarProgressoDownload(progresso, 
                        String.format("Baixando: %d%%", progresso));
                }

                String resposta = cliente.fazerDownload(nomeArquivo);
                
                SwingUtilities.invokeLater(() -> {
                    if (resposta.startsWith("OK|")) {
                        atualizarProgressoDownload(100, "Download concluído");
                        log(resposta.substring(3));
                        JOptionPane.showMessageDialog(ClienteFrame.this, resposta.substring(3), "Download Concluído", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        atualizarProgressoDownload(0, "Erro no download");
                        log("Erro no download: " + resposta);
                        JOptionPane.showMessageDialog(ClienteFrame.this, resposta, "Erro no Download", JOptionPane.ERROR_MESSAGE);
                    }
                });
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("Download interrompido");
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    atualizarProgressoDownload(0, "Erro no download");
                    log("Erro no download: " + e.getMessage());
                });
            }
        }).start();
    }

    private void deletarArquivo() {
        if (!conectado || cliente == null) {
            log("Não conectado ao servidor");
            return;
        }

        String nome = listaArquivos.getSelectedValue();

        if (nome == null) {
            log(""
                    + "Selecione um arquivo para deletar");
            JOptionPane.showMessageDialog(this, "Selecione um arquivo da lista", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja deletar '" + nome + "' do servidor?",
                "Confirmar Exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacao != JOptionPane.YES_OPTION) return;

        new Thread(() -> {
            try {
                log("Removendo: " + nome);

                String resposta = cliente.deletarArquivo(nome);

                if (resposta.startsWith("DELETE_OK")) {
                    log("Arquivo deletado com sucesso");
                    atualizarLista();
                } else {
                    log("Erro ao deletar: " + resposta);
                    JOptionPane.showMessageDialog(ClienteFrame.this, resposta, "Erro ao Deletar", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception e) {
                log("Erro ao deletar: " + e.getMessage());
                JOptionPane.showMessageDialog(ClienteFrame.this, "Erro: " + e.getMessage(), "Erro ao Deletar", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }

    private void habilitarBotoes(boolean estado) {
        btnUpload.setEnabled(estado);
        btnDownload.setEnabled(estado);
        btnDeletar.setEnabled(estado);
        btnAtualizar.setEnabled(estado);
    }

    private void atualizarProgressoDownload(int valor, String texto) {
        SwingUtilities.invokeLater(() -> {
            progressBarDownload.setValue(valor);
            progressBarDownload.setString(valor + "%");
            labelDownload.setText("Download: " + texto);
        });
    }

    private void atualizarProgressoUpload(int valor, String texto) {
        SwingUtilities.invokeLater(() -> {
            progressBarUpload.setValue(valor);
            progressBarUpload.setString(valor + "%");
            labelUpload.setText("Upload: " + texto);
        });
    }

    private void limparBarrasProgresso() {
        limparBarrasProgressoDownload();
        limparBarrasProgressoUpload();
    }

    private void limparBarrasProgressoDownload() {
        SwingUtilities.invokeLater(() -> {
            progressBarDownload.setValue(0);
            progressBarDownload.setString("0%");
            progressBarDownload.setForeground(new Color(0, 100, 200));
            labelDownload.setText("Download: Aguardando...");
        });
    }

    private void limparBarrasProgressoUpload() {
        SwingUtilities.invokeLater(() -> {
            progressBarUpload.setValue(0);
            progressBarUpload.setString("0%");
            progressBarUpload.setForeground(new Color(50, 150, 50));
            labelUpload.setText("Upload: Aguardando...");
        });
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            areaLog.append("[" +
                    java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
                    + "] " + msg + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
            new ClienteFrame().setVisible(true);
    }
}