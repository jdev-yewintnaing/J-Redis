package dev.yewintnaing.logic;

import dev.yewintnaing.protocol.RespArray;
import dev.yewintnaing.protocol.RespBulkString;
import dev.yewintnaing.storage.RedisStorage;

class ExpireCommand implements RedisCommand {
    @Override
    public String execute(RespArray args, dev.yewintnaing.handler.ClientHandler client) {

        if (args.elements().size() < 3) {
            return "-ERR wrong number of arguments for 'expire' command\r\n";
        }

        String key = ((RespBulkString) args.elements().get(1)).asUtf8();
        long seconds = Long.parseLong(((RespBulkString) args.elements().get(2)).asUtf8());

        return RedisStorage.setExpiry(key, seconds) ? ":1\r\n" : ":0\r\n";
    }

    @Override
    public boolean isWriteCommand() {

        return true;
    }

}
