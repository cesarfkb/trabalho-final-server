package com.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Server {
    public static final String ADDRESS = "localhost";
    public static final int PORT = 4000;
    private ServerSocket serverSocket;
    private final List<SocketClient> clients = new LinkedList<>();
    private CrudDB crudDB = new CrudDB();

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciado na porta: " + PORT);
        clientConnectionLoop();
    }

    private void clientConnectionLoop() throws IOException {
        System.out.println("Aguardando conexao de um cliente!");
        while (true) {
            SocketClient clientSocket = new SocketClient(serverSocket.accept());
            clients.add(clientSocket);
            new Thread(() -> clientOpLoop(clientSocket)).start();
        }
    }

    private void clientOpLoop(SocketClient clientSocket) {
        int op;
        try {
            while (true) {
                op = clientSocket.getOperation();
                System.out.println(op);
                switch (op) {
                    case 1:
                        salvarConteudo(clientSocket);
                        break;
                    case 2:
                        carregarConteudo(clientSocket);
                        break;
                    case 3:
                        login(clientSocket);
                        break;
                    case 4:
                        registrar(clientSocket);
                        break;
                    case 5:
                        pegarUltimoID(clientSocket);
                        break;
                    case 6:
                        pegarIdioma(clientSocket);
                        break;
                    case 7:
                        salvarIdioma(clientSocket);
                        break;
                    case -1:
                        break;
                    default:
                        System.out.println("Operacao invalida!");
                }
                if (op == -1) {
                    break;
                }
            }
        } finally {
            clientSocket.close();
        }
    }

    private void salvarIdioma(SocketClient clientSocket) {
        int data;
        try {
            data = clientSocket.getOneByte();
            System.out.println("DATA: " + data);
            crudDB.salvarIdioma(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pegarIdioma(SocketClient clientSocket) {
        byte[] data;
        try {
            data = clientSocket.getString();
            String user = new String(data);
            int id = crudDB.pegarIdioma(user);
            clientSocket.sendOneInt(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pegarUltimoID(SocketClient clientSocket) {
        byte[] data;
        try {
            data = clientSocket.getString();
            System.out.println(Arrays.toString(data));
            String user = new String(data);
            int id = crudDB.pegarUltimoID(user);
            System.out.println(id);
            clientSocket.sendOneInt(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registrar(SocketClient cliente) {
        try {
            byte[] data = cliente.getData();
            String infoLogin = new String(data);
            String[] info = infoLogin.split("\\+");
            String username = info[0];
            String password = info[1];
            User usuario = new User(username, password);
            // Verificação no CRUD
            boolean verificacao = crudDB.verificarUsuario(username);
            if (!verificacao) {
                cliente.sendOneByte((byte) '1');
                crudDB.insertUser(usuario);
            } else {
                cliente.sendOneByte((byte) '0');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void login(SocketClient cliente) {
        try {
            byte[] data = cliente.getData();
            String infoLogin = new String(data);
            String[] info = infoLogin.split("\\+");
            String user = info[0];
            String password = info[1];
            // Verificação no CRUD
            boolean verificacao = crudDB.verificarUsuario(user, password);
            if (verificacao) {
                cliente.sendOneByte((byte) '1');
            } else {
                cliente.sendOneByte((byte) '0');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void carregarConteudo(SocketClient cliente) {
        try {
            int id = cliente.getOneByte();
            Object[] dados = crudDB.pegarDadosGravacao(id);
            int tempoTotal = (int) dados[0];
            int[] tempos = (int[]) dados[1];
            ArrayList<String> textos = (ArrayList<String>) dados[2];

            cliente.sendString(String.valueOf(tempoTotal).getBytes());
            salvarDadosArray(tempos, cliente);
            salvarDadosArray(textos.toArray(new String[0]), cliente);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void salvarDadosArray(String[] array, SocketClient client) {
        byte[][] byteArrayDados = new byte[array.length + 1][];
        byteArrayDados[0] = new byte[] { (byte) array.length };
        int tamanhoByteArray = 1;
        for (int i = 1; i <= array.length; i++) {
            byte[] tamanhoArrayInterno = new byte[] { (byte) array[i - 1].getBytes().length };
            byteArrayDados[i] = Arrays.copyOf(tamanhoArrayInterno,
                    tamanhoArrayInterno.length + array[i - 1].getBytes().length);
            System.arraycopy(array[i - 1].getBytes(), 0, byteArrayDados[i], tamanhoArrayInterno.length,
                    array[i - 1].getBytes().length);
            tamanhoByteArray += byteArrayDados[i].length;
        }

        try {
            client.sendData(byteArrayDados, tamanhoByteArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void salvarDadosArray(int[] array, SocketClient client) {
        byte[][] byteArrayDados = new byte[array.length + 1][];
        byteArrayDados[0] = new byte[] { (byte) array.length };
        int tamanhoByteArray = 1;
        for (int i = 1; i <= array.length; i++) {
            byte[] tamanhoArrayInterno = new byte[] { (byte) String.valueOf(array[i - 1]).getBytes().length };
            byteArrayDados[i] = Arrays.copyOf(tamanhoArrayInterno,
                    tamanhoArrayInterno.length + String.valueOf(array[i - 1]).getBytes().length);
            System.arraycopy(String.valueOf(array[i - 1]).getBytes(), 0, byteArrayDados[i], tamanhoArrayInterno.length,
                    String.valueOf(array[i - 1]).getBytes().length);
            tamanhoByteArray += byteArrayDados[i].length;
        }

        try {
            client.sendData(byteArrayDados, tamanhoByteArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void salvarConteudo(SocketClient cliente) {
        byte[] buffer;
        try {
            buffer = cliente.getData();
            System.out.println("BUFFER: " + Arrays.toString(buffer));
            byte[][] bufferSeparado = separarBuffer(buffer, 4);
            String[] dados = new String[4];
            for (int i = 0; i < 4; i++) {
                dados[i] = new String(bufferSeparado[i]);
            }

            String usuario = dados[0];
            int id = Integer.parseInt(dados[1]);
            String local = dados[2];
            int tempo = Integer.parseInt(dados[3]);

            crudDB.id = id;
            crudDB.usuario = usuario;
            crudDB.local = local;
            crudDB.tempototal = tempo;
            
            buffer = cliente.getData();

            if (buffer.length == 1 || buffer.length == 0) {
                crudDB.salvarDadosGravacao();
                return;
            }

            bufferSeparado = separarBuffer(buffer);
            dados = new String[bufferSeparado.length];
            for (int i = 0; i < bufferSeparado.length; i++) {
                dados[i] = new String(bufferSeparado[i]);
            }

            ArrayList<Integer> tempos = new ArrayList<>();
            for (int i = 0; i < dados.length; i++) {
                tempos.add(Integer.parseInt(dados[i]));
            }

            buffer = cliente.getData();
            bufferSeparado = separarBuffer(buffer);
            ArrayList<String> textos = new ArrayList<>();
            for (int i = 0; i < bufferSeparado.length; i++) {
                textos.add(new String(bufferSeparado[i]));
            }

            System.out.println("ID NO SERVIDOR: " + id);

            crudDB.tempos = tempos;
            crudDB.textos = textos;
            crudDB.salvarDadosGravacao();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private byte[][] separarBuffer(byte[] buffer) {
        int tamanho = buffer[0];
        int indice = 1;
        byte[][] bufferSeparado = new byte[tamanho][];
        for (int i = 0; i < tamanho; i++) {
            int tamanhoArray = buffer[indice];
            bufferSeparado[i] = new byte[tamanhoArray];
            for (int j = 0; j < tamanhoArray; j++) {
                bufferSeparado[i][j] = buffer[indice + j + 1];
            }
            indice += tamanhoArray + 1;
        }
        System.out.println(bufferSeparado[0].toString());
        return bufferSeparado;

    }

    private byte[][] separarBuffer(byte[] buffer, int tamanho) {
        int indice = 0;
        byte[][] bufferSeparado = new byte[tamanho][];
        for (int i = 0; i < tamanho; i++) {
            int tamanhoArray = buffer[indice];
            bufferSeparado[i] = new byte[tamanhoArray];
            for (int j = 0; j < tamanhoArray; j++) {
                bufferSeparado[i][j] = buffer[indice + j + 1];
            }
            indice += tamanhoArray + 1;
        }
        return bufferSeparado;
    }

}
