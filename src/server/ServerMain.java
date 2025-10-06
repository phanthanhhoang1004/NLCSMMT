package src.server;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import src.model.Filedata;

public class ServerMain {
    private static final List<ClientHandler> connectedClients = new ArrayList<>();

    public static synchronized void addClient(ClientHandler handler) {
        connectedClients.add(handler);
    }

    public static synchronized List<ClientHandler> getConnectedClients() {
        return connectedClients;
    }

    public static synchronized void removeClient(ClientHandler handler) {
        connectedClients.remove(handler);
    }

    public static void main(String[] args) throws Exception {
        final int PORT = 5000;
        System.out.println("Server dang chay tren cong " + PORT + "...");
        connectedClients.clear();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Ket noi tu: " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket);
                ServerMain.addClient(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

}
