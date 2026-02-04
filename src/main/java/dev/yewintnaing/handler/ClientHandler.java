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

import static dev.yewintnaing.logic.CommandProcessor.handle;

public class ClientHandler {

    public static void handleClient(Socket socket) {

        try (socket;
             InputStream in = new BufferedInputStream(socket.getInputStream());
             OutputStream out = new BufferedOutputStream(socket.getOutputStream())) {

            while (true) {
                RespType request = RespParser.readResp(in);
                if (request == null) {
                    System.out.println("Client disconnected.");
                    break;
                }

                try {
                    if (!(request instanceof RespArray commandArray)) {
                        writeError(out, "Protocol error: expected array");
                        continue;
                    }

                    String respResponse = handle(commandArray);

                    out.write(respResponse.getBytes(StandardCharsets.UTF_8));
                    out.flush();

                } catch (Exception e) {
                    writeError(out, e.getMessage());
                    out.flush();
                    System.err.println("Protocol error: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        }
    }

    private static void writeError(OutputStream out, String msg) throws IOException {
        // RESP error: -ERR <msg>\r\n
        String safe = (msg == null || msg.isBlank()) ? "ERR" : msg;
        out.write(("-ERR " + safe + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

}
