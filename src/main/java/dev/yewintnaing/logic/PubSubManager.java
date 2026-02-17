package dev.yewintnaing.logic;

import dev.yewintnaing.handler.ClientHandler;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class PubSubManager {
    private static final PubSubManager INSTANCE = new PubSubManager();
    private final ConcurrentHashMap<String, Set<ClientHandler>> channels = new ConcurrentHashMap<>();

    private PubSubManager() {
    }

    public static PubSubManager getInstance() {
        return INSTANCE;
    }

    public void subscribe(String channel, ClientHandler client) {
        channels.computeIfAbsent(channel, k -> new CopyOnWriteArraySet<>()).add(client);
    }

    public void unsubscribe(String channel, ClientHandler client) {
        Set<ClientHandler> subscribers = channels.get(channel);
        if (subscribers != null) {
            subscribers.remove(client);
            if (subscribers.isEmpty()) {
                channels.remove(channel);
            }
        }
    }

    public void unsubscribeAll(ClientHandler client) {
        channels.forEach((channel, subscribers) -> {
            subscribers.remove(client);
            if (subscribers.isEmpty()) {
                channels.remove(channel);
            }
        });
    }

    public int publish(String channel, String message) {
        Set<ClientHandler> subscribers = channels.get(channel);
        if (subscribers == null || subscribers.isEmpty()) {
            return 0;
        }

        // RESPONSIBILITY: Redis Pub/Sub message format
        // *3\r\n$7\r\nmessage\r\n$<channel_len>\r\n<channel>\r\n$<msg_len>\r\n<message>\r\n
        String encodedMessage = "*3\r\n$7\r\nmessage\r\n$" + channel.length() + "\r\n" + channel + "\r\n$"
                + message.length() + "\r\n" + message + "\r\n";

        int count = 0;
        for (ClientHandler client : subscribers) {
            client.sendMessage(encodedMessage);
            count++;
        }
        return count;
    }
}
