package dev.yewintnaing.logic;

import dev.yewintnaing.protocol.RespArray;

class PingCommand implements RedisCommand {
    @Override
    public String execute(RespArray args, dev.yewintnaing.handler.ClientHandler client) {

        return "+PONG\r\n";
    }
}
