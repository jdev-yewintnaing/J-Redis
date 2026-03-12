package dev.yewintnaing.logic;

import dev.yewintnaing.protocol.RespArray;
import dev.yewintnaing.protocol.RespBulkString;
import dev.yewintnaing.storage.RedisStorage;

public class SMembersCommand implements RedisCommand {
    @Override
    public String execute(RespArray args, dev.yewintnaing.handler.ClientHandler client) {
        var elements = args.elements();
        if (elements.size() != 2) {
            return "-ERR wrong number of arguments for 'smembers' command\r\n";
        }

        String key = ((RespBulkString) elements.get(1)).asUtf8();

        try {
            return RedisStorage.smembers(key)
                    .map(set -> {
                        StringBuilder sb = new StringBuilder();
                        sb.append("*").append(set.size()).append("\r\n");
                        for (String member : set) {
                            sb.append("$").append(member.length()).append("\r\n").append(member).append("\r\n");
                        }
                        return sb.toString();
                    })
                    .orElse("*0\r\n");
        } catch (IllegalStateException e) {
            return "-ERR " + e.getMessage() + "\r\n";
        }
    }
}
