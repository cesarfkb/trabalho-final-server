package com.example;

import java.sql.*;
import java.util.ArrayList;

public class CrudDB {
    int id;
    String local, usuario;
    int tempototal;
    ArrayList<Integer> tempos;
    ArrayList<String> textos;
    int optIdioma;

    public static void criarTableConsultas() {
        Connection conn = ConexaoDB.conectarAoBancoConsultas();
        Statement stmt = null;
        String sql = "CREATE TABLE IF NOT EXISTS gravacoes " +
                "(user VARCHAR(250) NOT NULL, " + 
                "id INT NOT NULL, " +
                "local VARCHAR(150) NOT NULL, " +
                "tempototal INT NOT NULL, " +
                "tempomarcacoes JSON NULL, " +
                "textomarcacoes JSON NULL)";
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void criarTableUsuarios() {
        Connection conn = ConexaoDB.conectarAoBancoConsultas();
        Statement stmt = null;
        String sql = "CREATE TABLE IF NOT EXISTS users " +
                "(username VARCHAR(255) NOT NULL PRIMARY KEY, " +
                "password VARCHAR(150) NOT NULL, " +
                "idioma INT NOT NULL) ";
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int pegarUltimoID(String user) {
        usuario = user;
        String sql = "SELECT MAX(id) FROM gravacoes WHERE user=" + "\"" + user + "\"";
        ResultSet rs = null;
        int id = 1;
        try {
            rs = pegarResultados(sql);
            if (rs.next()) {
                id = rs.getInt(1) + 1;
            }
            return id;
        } catch (SQLException e) {
            return id;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int pegarIdioma(String user) {
        String sql = "SELECT idioma FROM users WHERE username=" + "\"" + user + "\"";
        ResultSet rs = null;
        try {
            rs = pegarResultados(sql);
            if (rs.next()) {
                optIdioma = rs.getInt(1);
            }
            return optIdioma;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public void salvarDadosGravacao() {
        Connection conn = ConexaoDB.conectarAoBancoConsultas();
        String sql = "INSERT INTO gravacoes(id, local, tempototal, tempomarcacoes, textomarcacoes, user) VALUES(?,?,?,?,?,?)";
        PreparedStatement stmt = null;
        String tempoString = "";
        if (tempoString.isEmpty() || tempoString.isBlank() || tempoString.length() == 0) {
            tempoString = "[]";
        } else {
            tempoString = tempos.toString();
        }

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, String.valueOf(id));
            stmt.setString(2, local);
            stmt.setString(3, String.valueOf(tempototal));
            stmt.setString(4, tempoString);
            stmt.setString(5, stringArrayToString(textos));
            stmt.setString(6, usuario);

            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Object[] pegarDadosGravacao(int id) {
        String sql = "SELECT tempototal,tempomarcacoes,textomarcacoes FROM gravacoes WHERE id=" + id + " and user=" + "\"" + usuario + "\"";
        System.out.println(sql);
        ResultSet rs = pegarResultados(sql);
        try {
            if (rs.next()) {
                int tempoTotal = rs.getInt(1);
                String tempos = rs.getString(2);
                String stringAnotacoes = rs.getString(3);
                
                int[] temposArray = stringToIntArray(tempos);
                ArrayList<String> anotacoes = stringAnotacoesParaArray(stringAnotacoes, temposArray.length);

                return new Object[] { tempoTotal, temposArray, anotacoes };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void insertUser(User user) {
        Connection conn = ConexaoDB.conectarAoBancoConsultas();
        String sql = "INSERT INTO users (username, password, idioma) VALUES (?, ?, ?)";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            String encryptedPassword = user.getEncrypter().encryptPassword(user.getPassword());
            stmt.setString(2, encryptedPassword);
            stmt.setInt(3, 0);
            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void selectUser(User user) {
        Connection conn = ConexaoDB.conectarAoBancoConsultas();
        String sql = "SELECT * FROM users WHERE username = ?";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("Username: " + rs.getString("username"));
                System.out.println("Password: " + user.decryptPassword(rs.getString("password")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            conn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int[] stringToIntArray(String s) {
        String[] array = s.substring(1, s.length() - 1).split(", ");
        int[] intArray = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            try {
                intArray[i] = Integer.parseInt(array[i]);
            } catch (NumberFormatException e) {
                return new int[] { -1 };
            }
        }
        return intArray;
    }

    private ArrayList<String> stringAnotacoesParaArray(String texto, int lengthTempos) {
        ArrayList<String> anotacoes = new ArrayList<>();
        String textoAjuste = texto.substring(1, texto.length() - 1);
        String[] anotacoesArray = textoAjuste.split(", ");
        if (anotacoesArray.length == lengthTempos) {
            if (!anotacoesArray[0].equals("")) {
                for (String anotacao : anotacoesArray) {
                    anotacoes.add(anotacao.substring(2, anotacao.length() - 2));
                }
            }
            return anotacoes;
        } else {
            for (int i = 0; i < anotacoesArray.length; i++) {
                if (verificaInicio(anotacoesArray[i])) {
                    if (verificaFim(anotacoesArray[i])) {
                        anotacoes.add(anotacoesArray[i].substring(2, anotacoesArray[i].length() - 2));
                    } else {
                        String anotacao = anotacoesArray[i].substring(2);
                        for (int j = i + 1; j < anotacoesArray.length; j++) {
                            if (verificaFim(anotacoesArray[j])) {
                                anotacao += anotacoesArray[j].substring(0, anotacoesArray[j].length() - 2);
                                anotacoes.add(anotacao);
                                i = j;
                                break;
                            } else {
                                anotacao += anotacoesArray[j];
                            }
                        }
                    }
                }
            }
            return anotacoes;
        }
    }

    private boolean verificaInicio(String s) {
        return s.substring(0, 2).equals("\">");
    }

    private boolean verificaFim(String s) {
        return s.substring(s.length() - 2).equals("<\"");
    }

    private String stringArrayToString(ArrayList<String> textos) {
        String textosAjuste = "[";
        try {
        for (int i = 0; i < textos.size(); i++) {
            textosAjuste = textosAjuste + "\">" + textos.get(i) + "<\"";
            if (i < textos.size() - 1) {
                textosAjuste = textosAjuste + ", ";
            }
        }} catch (NullPointerException e) {
            return "[]";
        }

        textosAjuste += "]";
        return textosAjuste;
    }

    private ResultSet pegarResultados(String sql) {
        Connection conn = ConexaoDB.conectarAoBancoConsultas();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            return rs;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e.printStackTrace();
            }
        }
        try {
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean verificarUsuario(String username) {
        Connection conn = ConexaoDB.conectarAoBancoConsultas();
        String sql = "SELECT * FROM users WHERE username = ?";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean verificarUsuario(String user, String password) {
        User usuario = new User(user, password);
        Connection conn = ConexaoDB.conectarAoBancoConsultas();
        String sql = "SELECT * FROM users WHERE username = ?";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user);
            rs = stmt.executeQuery();
            if (rs.next()) {
                String encryptedPassword = rs.getString("password");
                if (encryptedPassword.equals(usuario.getEncrypter().encryptPassword(password))) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void salvarIdioma(int data) {
        Connection conn = ConexaoDB.conectarAoBancoConsultas();
        String sql = "UPDATE users SET idioma=? WHERE username=?";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, data);
            stmt.setString(2, usuario);
            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}