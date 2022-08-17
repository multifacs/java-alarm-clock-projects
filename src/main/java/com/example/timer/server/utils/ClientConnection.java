package com.example.timer.server.utils;

import com.example.timer.data.OpCodes;
import com.example.timer.server.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientConnection {

    private final Socket socket;
    private final ObjectInputStream fromClient;
    private final ObjectOutputStream toClient;

    public ClientConnection(Socket socket) {
        this.socket = socket;

        ObjectInputStream tryFrom = null;
        ObjectOutputStream tryTo = null;

        try {
            tryFrom = new ObjectInputStream(socket.getInputStream());
            tryTo = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.fromClient = tryFrom;
            this.toClient = tryTo;
        }
    }

    public void sendMessage(OpCodes opcode, Object s) {
        synchronized (toClient) {
            try {
                toClient.writeObject(opcode.ordinal());
                toClient.writeObject(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public Object receiveMessage() {
        try {
            return fromClient.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // e.printStackTrace();
            System.out.println("Socket closed");
        }
        return null;
    }

    public boolean isClosed() {
        return socket.isClosed();
    }
}
