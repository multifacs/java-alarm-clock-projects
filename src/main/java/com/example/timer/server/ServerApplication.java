package com.example.timer.server;

public class ServerApplication {

    private final Server server = new Server();

    public static void main(String[] args) {

        ServerApplication serverApplication = new ServerApplication();
        int PORT = 8888;
        serverApplication.server.start(PORT);
    }
}