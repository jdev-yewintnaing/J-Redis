package dev.yewintnaing.logic;

import dev.yewintnaing.protocol.RespArray;
import dev.yewintnaing.protocol.RespBulkString;
import dev.yewintnaing.storage.RedisStorage;

public class SCardCommand implements RedisCommand {
    @Override
    public String execute(RespArray args, dev.yewintnaing.handler.ClientHandler client) {
        var elements = args.elements();
        if (elements.size() != 2) {
            return "-ERR wrong number of arguments for 'scard' command\r\n";
        }

        String key = ((RespBulkString) elements.get(1)).asUtf8();

        try {
            int count = RedisStorage.scard(key);
            return ":" + count + "\r\n";
        } catch (IllegalStateException e) {
            return "-ERR " + e.getMessage() + "\r\n";
        }
    }
}
