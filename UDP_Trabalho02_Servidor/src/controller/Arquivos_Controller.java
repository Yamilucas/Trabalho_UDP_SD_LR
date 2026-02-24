package controller;

import util.Conexao;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Arquivos_Controller {
    private static final String PASTA_ARQUIVOS = "arquivos_servidor/";
    
    public Arquivos_Controller() {
        new File(PASTA_ARQUIVOS).mkdirs();
    }
    
    public boolean salvarArquivo(String nomeArquivo, long tamanho, byte[] dados) {
        Conexao conn = new Conexao();
        
        try {
            String caminho = PASTA_ARQUIVOS + nomeArquivo;
            FileOutputStream fos = new FileOutputStream(caminho);
            fos.write(dados);
            fos.close();
            
            conn.conectar();
            String sql = "INSERT INTO arquivos (nome, tamanho, caminho) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.conector.prepareStatement(sql);
            stmt.setString(1, nomeArquivo);
            stmt.setLong(2, tamanho);
            stmt.setString(3, caminho);
            
            int resultado = stmt.executeUpdate();
            return resultado > 0;
            
        } catch (Exception e) {
            System.out.println("Erro salvando arquivo: " + e.getMessage());
            return false;
        } finally {
            conn.desconectar();
        }
    }
    
    public byte[] buscarArquivo(String nomeArquivo) {
        Conexao conn = new Conexao();
        
        try {
            conn.conectar();
            String sql = "SELECT caminho FROM arquivos WHERE nome = ?";
            PreparedStatement stmt = conn.conector.prepareStatement(sql);
            stmt.setString(1, nomeArquivo);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String caminho = rs.getString("caminho");
                return Files.readAllBytes(Paths.get(caminho));
            }
            return null;
            
        } catch (Exception e) {
            System.out.println("Erro buscando arquivo: " + e.getMessage());
            return null;
        } finally {
            conn.desconectar();
        }
    }
    
    public List<String> listarArquivos() {
        List<String> arquivos = new ArrayList<>();
        Conexao conn = new Conexao();
        
        try {
            conn.conectar();
            String sql = "SELECT nome, tamanho FROM arquivos ORDER BY nome";
            PreparedStatement stmt = conn.conector.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String nome = rs.getString("nome");
                long tamanho = rs.getLong("tamanho");
                arquivos.add(nome + "|" + tamanho);
            }
            
        } catch (Exception e) {
            System.out.println("Erro listando arquivos: " + e.getMessage());
        } finally {
            conn.desconectar();
        }
        return arquivos;
    }
    
    public boolean deletarArquivo(String nomeArquivo) {
        Conexao conn = new Conexao();
        
        try {
            conn.conectar();
            String sqlBuscar = "SELECT caminho FROM arquivos WHERE nome = ?";
            PreparedStatement stmtBuscar = conn.conector.prepareStatement(sqlBuscar);
            stmtBuscar.setString(1, nomeArquivo);
            
            ResultSet rs = stmtBuscar.executeQuery();
            if (rs.next()) {
                String caminho = rs.getString("caminho");
                // Deletar arquivo físico
                Files.deleteIfExists(Paths.get(caminho));
            }
            
            String sqlDeletar = "DELETE FROM arquivos WHERE nome = ?";
            PreparedStatement stmtDeletar = conn.conector.prepareStatement(sqlDeletar);
            stmtDeletar.setString(1, nomeArquivo);
            
            int resultado = stmtDeletar.executeUpdate();
            return resultado > 0;
            
        } catch (Exception e) {
            System.out.println("Erro deletando arquivo: " + e.getMessage());
            return false;
        } finally {
            conn.desconectar();
        }
    }
}