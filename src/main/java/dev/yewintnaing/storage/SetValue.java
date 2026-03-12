package dev.yewintnaing.storage;

import java.util.Set;

public record SetValue(Set<String> value, long expiryTime) implements RedisValue {
    @Override
    public long expiryTime() {
        return expiryTime;
    }
}
