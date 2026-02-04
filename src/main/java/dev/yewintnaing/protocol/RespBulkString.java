package dev.yewintnaing.protocol;

import java.nio.charset.StandardCharsets;

// All records must be public to be accessed by the Parser and Logic packages
public record RespBulkString(byte[] data) implements RespType {
    public String asUtf8() { return new String(data, StandardCharsets.UTF_8); }
}