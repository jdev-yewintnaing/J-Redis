package dev.yewintnaing.handler;

import dev.yewintnaing.protocol.RespArray;
import dev.yewintnaing.protocol.RespParser;
import dev.yewintnaing.protocol.RespType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import dev.yewintnaing.logic.CommandProcessor;
import dev.yewintnaing.logic.PubSubManager;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private OutputStream out;
    private final CommandProcessor commandProcessor;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.commandProcessor = new CommandProcessor();
    }

    @Override
    public void run() {
        try (socket;
                InputStream in = new BufferedInputStream(socket.getInputStream());
                OutputStream out = new BufferedOutputStream(socket.getOutputStream())) {

            this.out = out;

            while (!socket.isClosed()) {
                RespType request = RespParser.readResp(in);
                if (request == null) {
                    break;
                } // Client disconnected

                try {
                    if (!(request instanceof RespArray commandArray)) {
                        writeError("Protocol error: expected array");
                        continue;
                    }

                    String respResponse = commandProcessor.handle(commandArray, this);
                    sendMessage(respResponse);

                } catch (Exception e) {
                    writeError(e.getMessage());
                    System.err.println("Protocol error: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        } finally {
            // Cleanup subscriptions if necessary
            PubSubManager.getInstance().unsubscribeAll(this);
        }
    }

    public synchronized void sendMessage(String message) {
        if (out == null || socket.isClosed())
            return;
        try {
            out.write(message.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message to client: " + e.getMessage());
            try {
                socket.close();
            } catch (IOException ex) {
                // Ignore
            }
        }
    }

    private void writeError(String msg) {
        String safe = (msg == null || msg.isBlank()) ? "ERR" : msg;
        sendMessage("-ERR " + safe + "\r\n");
    }

}
