package dev.yewintnaing.logic;

import dev.yewintnaing.protocol.RespArray;
import dev.yewintnaing.protocol.RespBulkString;
import dev.yewintnaing.storage.RedisStorage;

public class SAddCommand implements RedisCommand {
    @Override
    public String execute(RespArray args, dev.yewintnaing.handler.ClientHandler client) {
        var elements = args.elements();
        if (elements.size() < 3) {
            return "-ERR wrong number of arguments for 'sadd' command\r\n";
        }

        String key = ((RespBulkString) elements.get(1)).asUtf8();
        String[] members = elements.stream()
                .skip(2)
                .map(e -> ((RespBulkString) e).asUtf8())
                .toArray(String[]::new);

        try {
            int added = RedisStorage.sadd(key, members);
            return ":" + added + "\r\n";
        } catch (IllegalStateException e) {
            return "-ERR " + e.getMessage() + "\r\n";
        }
    }

    @Override
    public boolean isWriteCommand() {
        return true;
    }
}
