package dev.yewintnaing;

import dev.yewintnaing.logic.CommandProcessor;
import dev.yewintnaing.storage.CleanerTask;
import dev.yewintnaing.storage.PersistenceManager;

import dev.yewintnaing.handler.ClientHandler;
import java.io.*;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

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

                executor.submit(new ClientHandler(client));
            }

        }

    }

    public static void recovery() throws IOException {
        CommandProcessor processor = new CommandProcessor();
        for (var data : PersistenceManager.readAof()) {
            // AOF replay doesn't have a real client, and write commands don't utilize it
            // currently
            processor.handle(data, null);
        }
    }

}