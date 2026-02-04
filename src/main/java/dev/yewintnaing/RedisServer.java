package dev.yewintnaing;

import dev.yewintnaing.logic.CommandProcessor;
import dev.yewintnaing.storage.CleanerTask;
import dev.yewintnaing.storage.PersistenceManager;

import java.io.*;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

import static dev.yewintnaing.handler.ClientHandler.handleClient;

public class RedisServer {
    public static void main(String[] args) throws IOException {

        int port = 6379;

        recovery();


        CleanerTask.init();

        try (var serverSocket = new ServerSocket(port);
                var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            System.out.println("J-Redis is running on port " + port);

            while (true) {
                var client = serverSocket.accept();

                executor.submit(() -> handleClient(client));
            }

        }

    }

    public static void recovery() throws IOException {
        for (var data : PersistenceManager.readAof()){
            CommandProcessor.handle(data);
        }
    }

}