package dev.yewintnaing.logic;

import dev.yewintnaing.protocol.RespArray;

public interface RedisCommand {
    String execute(RespArray args, dev.yewintnaing.handler.ClientHandler client);

    default boolean isWriteCommand() {
        return false;
    }
}