package com.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        ConexaoDB.criarBanco();
        CrudDB.criarTableConsultas();
        CrudDB.criarTableUsuarios();
        System.out.println("CONSOLE DO SERVIDOR");
        try {
            Server server = new Server();
            server.start();
        } catch (IOException e) {
            System.out.println("Erro ao iniciar servidor: " + e.getMessage());
        }
        System.out.println("Servidor finalizado!");
    }
}
