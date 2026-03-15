package dev.yewintnaing.logic;

import dev.yewintnaing.protocol.RespArray;
import dev.yewintnaing.protocol.RespBulkString;
import dev.yewintnaing.storage.RedisStorage;

public class RPushCommand implements RedisCommand {
    @Override
    public String execute(RespArray args, dev.yewintnaing.handler.ClientHandler client) {

        if (args.elements().size() < 3) {
            return "-ERR RPUSH requires key and value\r\n";
        }

        String key = ((RespBulkString) args.elements().get(1)).asUtf8();
        String val = ((RespBulkString) args.elements().get(2)).asUtf8();

        RedisStorage.pushListRight(key, val);

        return "+OK\r\n";
    }

    @Override
    public boolean isWriteCommand() {
        return true;
    }
}
