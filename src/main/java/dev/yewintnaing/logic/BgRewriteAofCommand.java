package dev.yewintnaing.logic;

import dev.yewintnaing.handler.ClientHandler;
import dev.yewintnaing.protocol.RespArray;
import dev.yewintnaing.storage.PersistenceManager;

public class BgRewriteAofCommand implements RedisCommand {
    @Override
    public String execute(RespArray args, ClientHandler client) {
        // Run in background to avoid blocking the command thread
        Thread.ofVirtual().start(PersistenceManager::rewriteAof);
        return "+Background rewrite started\r\n";
    }
}
