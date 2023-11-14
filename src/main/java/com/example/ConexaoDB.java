package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoDB {

    static String servidor = "localhost";
    static String porta = "3306";
    static String usuario = "root";
    static String senha = "561326162"; // COLOCAR SENHA AQUI

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void criarBanco() {
        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://" + servidor + ":" + porta + "/?user=" + usuario + "&password=" + senha);
            conn.createStatement().executeUpdate("CREATE DATABASE IF NOT EXISTS consultas");
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar banco de dados: " + e);
        }
    }

    public static Connection conectarAoBancoConsultas() {
        try {
            return DriverManager.getConnection(
                    "jdbc:mysql://" + servidor + ":" + porta + "/consultas?user=" + usuario + "&password=" + senha);
        } catch (SQLException e) {
            throw new RuntimeException("Erro na conexao: " + e);
        }
    }
}
