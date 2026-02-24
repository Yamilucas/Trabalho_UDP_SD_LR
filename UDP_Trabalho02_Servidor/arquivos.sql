create database servidor_arquivos;
use servidor_arquivos;

CREATE TABLE arquivos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE,
    tamanho BIGINT NOT NULL,
    caminho VARCHAR(500) NOT NULL,
    data_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);