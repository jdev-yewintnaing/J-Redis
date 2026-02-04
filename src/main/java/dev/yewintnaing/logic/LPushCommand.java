package dev.yewintnaing.logic;

import dev.yewintnaing.protocol.RespArray;
import dev.yewintnaing.protocol.RespBulkString;
import dev.yewintnaing.storage.RedisStorage;

public class LPushCommand implements RedisCommand {
    @Override
    public String execute(RespArray args) {

        if (args.elements().size() < 3)
            return "-ERR LPUSH requires key and value\r\n";

        String key = ((RespBulkString) args.elements().get(1)).asUtf8();
        String val = ((RespBulkString) args.elements().get(2)).asUtf8();

        RedisStorage.pushList(key, val);

        return "+OK\r\n";
    }

    @Override
    public boolean isWriteCommand() {
        return true;
    }

}
