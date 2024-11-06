package com.shab.javaproject;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static Set<Socket> clientSockets = new HashSet<>();
    private static Set<String> clientNames = new HashSet<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Server is running...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected: " + clientSocket);

            // Create a new thread to handle this client
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            new Thread(clientHandler).start();
        }
    }

    // Method to broadcast messages to all clients
    public static synchronized void broadcastMessage(String message, Socket sender) throws IOException {
        for (Socket socket : clientSockets) {
            if (socket != sender) {
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeUTF(message);
            }
        }
    }

    // Register a new client socket
    public static synchronized void addClient(Socket clientSocket, String clientName) {
        clientSockets.add(clientSocket);
        clientNames.add(clientName);
    }

    // Remove a client from the chat
    public static synchronized void removeClient(Socket clientSocket, String clientName) throws IOException {
        clientSockets.remove(clientSocket);
        clientNames.remove(clientName);
        clientSocket.close();
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String clientName;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            // Get client's name
            out.writeUTF("Enter your name: ");
            clientName = in.readUTF();
            ChatServer.addClient(clientSocket, clientName);
            ChatServer.broadcastMessage(clientName + " has joined the chat.", clientSocket);

            // Receive and broadcast messages
            String message;
            while (true) {
                message = in.readUTF();
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
                ChatServer.broadcastMessage(clientName + ": " + message, clientSocket);
            }

            // Client disconnected
            ChatServer.broadcastMessage(clientName + " has left the chat.", clientSocket);
            ChatServer.removeClient(clientSocket, clientName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
