package dev.yewintnaing.logic;

import dev.yewintnaing.handler.ClientHandler;
import dev.yewintnaing.protocol.RespArray;
import dev.yewintnaing.protocol.RespBulkString;

public class PublishCommand implements RedisCommand {
    @Override
    public String execute(RespArray args, ClientHandler client) {
        if (args.elements().size() != 3) {
            return "-ERR wrong number of arguments for 'publish' command\r\n";
        }

        String channel = ((RespBulkString) args.elements().get(1)).asUtf8();
        String message = ((RespBulkString) args.elements().get(2)).asUtf8();

        int receivers = PubSubManager.getInstance().publish(channel, message);

        return ":" + receivers + "\r\n";
    }

    // PUBLISH is technically a write command in terms of activity, but doesn't
    // modify KV store.
    // We can leave isWriteCommand default false unless we want it in AOF.
    // Redis usually replicates PUBLISH. For this task, we might skip AOF for
    // simplicity unless requested.
}
