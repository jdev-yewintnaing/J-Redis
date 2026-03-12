package dev.yewintnaing.logic;

import dev.yewintnaing.protocol.RespArray;
import dev.yewintnaing.protocol.RespBulkString;
import dev.yewintnaing.storage.RedisStorage;

public class SIsMemberCommand implements RedisCommand {
    @Override
    public String execute(RespArray args, dev.yewintnaing.handler.ClientHandler client) {
        var elements = args.elements();
        if (elements.size() != 3) {
            return "-ERR wrong number of arguments for 'sismember' command\r\n";
        }

        String key = ((RespBulkString) elements.get(1)).asUtf8();
        String member = ((RespBulkString) elements.get(2)).asUtf8();

        try {
            boolean isMember = RedisStorage.sismember(key, member);
            return isMember ? ":1\r\n" : ":0\r\n";
        } catch (IllegalStateException e) {
            return "-ERR " + e.getMessage() + "\r\n";
        }
    }
}
