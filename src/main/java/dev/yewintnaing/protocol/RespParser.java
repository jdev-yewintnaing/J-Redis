package dev.yewintnaing.protocol;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class RespParser {

    private RespParser() { }

    /**
     * Reads one RESP value from the stream.
     *
     * @return RespType, or null if EOF before any prefix byte is read.
     */
    public static RespType readResp(InputStream in) throws IOException {

        int prefix = in.read();
        if (prefix == -1) {
            return null; // clean EOF
        }

        return switch (prefix) {
            case '+' -> new RespSimpleString(readLineCRLF(in));           // +OK\r\n
            case '-' -> new RespError(readLineCRLF(in));                  // -ERR ...\r\n
            case ':' -> new RespInteger(parseLongAscii(readLineCRLF(in))); // :1\r\n
            case '$' -> readBulkString(in);                               // $len\r\n...\r\n
            case '*' -> readArray(in);                                    // *count\r\n...
            default -> throw new IOException("Unknown RESP prefix: " + (char) prefix);
        };
    }

    private static RespType readBulkString(InputStream in) throws IOException {

        int len = parseIntAscii(readLineCRLF(in)); // ASCII digits
        if (len == -1) { return RespNullBulkString.INSTANCE; }

        byte[] data = readExact(in, len);
        expectCRLF(in);
        return new RespBulkString(data);
    }

    private static RespType readArray(InputStream in) throws IOException {

        int count = parseIntAscii(readLineCRLF(in));
        if (count == -1) { return RespNullArray.INSTANCE; }

        List<RespType> elements = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            RespType e = readResp(in);
            if (e == null) { throw new EOFException("EOF while reading array element " + i + "/" + count); }
            elements.add(e);
        }
        return new RespArray(elements);
    }


    /**
     * Reads bytes until CRLF and returns the line content decoded as UTF-8 (without CRLF).
     * This is safe for RESP header lines (numbers, simple strings, errors).
     */
    private static String readLineCRLF(InputStream in) throws IOException {

        ByteArrayOutputStream buf = new ByteArrayOutputStream(64);

        int prev = -1;
        while (true) {
            int cur = in.read();
            if (cur == -1) { throw new EOFException("EOF while reading CRLF line"); }

            buf.write(cur);

            if (prev == '\r' && cur == '\n') {
                byte[] all = buf.toByteArray();
                // drop the last 2 bytes (\r\n)
                int contentLen = all.length - 2;
                return new String(all, 0, contentLen, StandardCharsets.UTF_8);
            }
            prev = cur;
        }
    }

    private static void expectCRLF(InputStream in) throws IOException {

        int c1 = in.read();
        int c2 = in.read();
        if (c1 != '\r' || c2 != '\n') {
            throw new IOException("Expected CRLF after bulk string, got: "
                                      + printableByte(c1) + " " + printableByte(c2));
        }
    }

    private static byte[] readExact(InputStream in, int len) throws IOException {

        if (len < 0) { throw new IOException("Negative length: " + len); }

        byte[] buf = new byte[len];
        int off = 0;
        while (off < len) {
            int r = in.read(buf, off, len - off);
            if (r == -1) { throw new EOFException("EOF while reading " + len + " bytes (got " + off + ")"); }
            off += r;
        }
        return buf;
    }

    private static int parseIntAscii(String s) throws IOException {

        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid integer line: [" + s + "]", e);
        }
    }

    private static long parseLongAscii(String s) throws IOException {

        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid long line: [" + s + "]", e);
        }
    }

    private static String printableByte(int b) {

        if (b == -1) { return "EOF"; }
        if (b >= 32 && b <= 126) { return "'" + (char) b + "'"; }
        return "0x" + Integer.toHexString(b & 0xff);
    }

}