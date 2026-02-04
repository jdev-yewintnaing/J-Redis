package dev.yewintnaing.storage;

import dev.yewintnaing.protocol.RespArray;
import dev.yewintnaing.protocol.RespBulkString;
import dev.yewintnaing.protocol.RespParser;
import dev.yewintnaing.protocol.RespType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class PersistenceManager {

    private static final Path PATH_OF_AOF = Paths.get("appendOnlyFile.aof");

    private static final OutputStream AOF_OUT;

    static {
        try {
            AOF_OUT = new BufferedOutputStream(
                Files.newOutputStream(
                    PATH_OF_AOF,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
                                     )
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to open appendOnlyFile.aof", e);
        }
    }

    public static synchronized void log(RespArray respArray) {

        try {
            AOF_OUT.write(convertToRespBytes(respArray));
            AOF_OUT.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to appendOnlyFile.aof", e);
        }
    }

    private static byte[] convertToRespBytes(RespArray command) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        List<RespType> elements = command.elements();
        out.write(("*" + elements.size() + "\r\n").getBytes(StandardCharsets.US_ASCII));

        for (RespType element : elements) {
            if (!(element instanceof RespBulkString(byte[] data))) {
                throw new IOException(
                    "Only bulk string elements supported, got: " + element.getClass().getSimpleName());
            }

            out.write(("$" + data.length + "\r\n").getBytes(StandardCharsets.US_ASCII));
            out.write(data);
            out.write("\r\n".getBytes(StandardCharsets.US_ASCII));
        }

        return out.toByteArray();
    }

    public static List<RespArray> readAof() throws IOException {

        List<RespArray> commands = new ArrayList<>();

        try (InputStream in = new BufferedInputStream(Files.newInputStream(PATH_OF_AOF))) {
            RespType resp;
            while ((resp = RespParser.readResp(in)) != null) {
                if (!(resp instanceof RespArray arr)) {
                    throw new IOException("Invalid AOF entry: expected array, got " + resp.getClass().getSimpleName());
                }
                commands.add(arr);
            }
        }
        return commands;
    }

}
