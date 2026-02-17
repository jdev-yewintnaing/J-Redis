package dev.yewintnaing.logic;

import dev.yewintnaing.protocol.RespArray;
import dev.yewintnaing.protocol.RespBulkString;
import dev.yewintnaing.storage.RedisStorage;

public class LRangeCommand implements RedisCommand {
    @Override
    public String execute(RespArray args, dev.yewintnaing.handler.ClientHandler client) {

        var elements = args.elements();

        if (elements.get(1) instanceof RespBulkString e) {
            String key = e.toString();
            long start = Long.parseLong(((RespBulkString) elements.get(2)).asUtf8());
            long stop = Long.parseLong(((RespBulkString) elements.get(3)).asUtf8());

            return RedisStorage.getListRange(key, start, stop)
                    .map(list -> {
                        StringBuilder sb = new StringBuilder();
                        sb.append("*").append(list.size()).append("\r\n");
                        for (String item : list) {
                            sb.append("$").append(item.length()).append("\r\n")
                                    .append(item).append("\r\n");
                        }
                        return sb.toString();
                    })
                    .orElse("*0\r\n");

        }

        return "";
    }
}
