package dev.yewintnaing.logic;

import dev.yewintnaing.protocol.RespArray;
import dev.yewintnaing.protocol.RespBulkString;
import dev.yewintnaing.storage.RedisStorage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SetCommandTest {

    @Test
    void testSAddAndSMembers() {
        String key = "myset";
        SAddCommand sadd = new SAddCommand();
        
        // Add new members
        String res1 = sadd.execute(new RespArray(List.of(
                new RespBulkString("SADD".getBytes()),
                new RespBulkString(key.getBytes()),
                new RespBulkString("m1".getBytes()),
                new RespBulkString("m2".getBytes())
        )), null);
        assertEquals(":2\r\n", res1);

        // Add duplicate and new member
        String res2 = sadd.execute(new RespArray(List.of(
                new RespBulkString("SADD".getBytes()),
                new RespBulkString(key.getBytes()),
                new RespBulkString("m2".getBytes()),
                new RespBulkString("m3".getBytes())
        )), null);
        assertEquals(":1\r\n", res2);

        SMembersCommand smembers = new SMembersCommand();
        String membersRes = smembers.execute(new RespArray(List.of(
                new RespBulkString("SMEMBERS".getBytes()),
                new RespBulkString(key.getBytes())
        )), null);

        assertTrue(membersRes.contains("m1"));
        assertTrue(membersRes.contains("m2"));
        assertTrue(membersRes.contains("m3"));
        assertTrue(membersRes.startsWith("*3\r\n"));
    }

    @Test
    void testSRemAndSIsMember() {
        String key = "myset2";
        RedisStorage.sadd(key, "m1", "m2", "m3");

        SIsMemberCommand sismember = new SIsMemberCommand();
        assertEquals(":1\r\n", sismember.execute(new RespArray(List.of(
                new RespBulkString("SISMEMBER".getBytes()),
                new RespBulkString(key.getBytes()),
                new RespBulkString("m1".getBytes())
        )), null));
        assertEquals(":0\r\n", sismember.execute(new RespArray(List.of(
                new RespBulkString("SISMEMBER".getBytes()),
                new RespBulkString(key.getBytes()),
                new RespBulkString("m4".getBytes())
        )), null));

        SRemCommand srem = new SRemCommand();
        String remRes = srem.execute(new RespArray(List.of(
                new RespBulkString("SREM".getBytes()),
                new RespBulkString(key.getBytes()),
                new RespBulkString("m1".getBytes()),
                new RespBulkString("m4".getBytes())
        )), null);
        assertEquals(":1\r\n", remRes);

        assertEquals(":0\r\n", sismember.execute(new RespArray(List.of(
                new RespBulkString("SISMEMBER".getBytes()),
                new RespBulkString(key.getBytes()),
                new RespBulkString("m1".getBytes())
        )), null));
    }

    @Test
    void testSCard() {
        String key = "myset3";
        RedisStorage.sadd(key, "m1", "m2");

        SCardCommand scard = new SCardCommand();
        assertEquals(":2\r\n", scard.execute(new RespArray(List.of(
                new RespBulkString("SCARD".getBytes()),
                new RespBulkString(key.getBytes())
        )), null));
    }

    @Test
    void testSetTTL() throws InterruptedException {
        String key = "ttlset";
        RedisStorage.sadd(key, "m1");
        
        RedisStorage.setExpiry(key, 1);
        
        SCardCommand scard = new SCardCommand();
        assertEquals(":1\r\n", scard.execute(new RespArray(List.of(
                new RespBulkString("SCARD".getBytes()),
                new RespBulkString(key.getBytes())
        )), null));

        Thread.sleep(1100);

        assertEquals(":0\r\n", scard.execute(new RespArray(List.of(
                new RespBulkString("SCARD".getBytes()),
                new RespBulkString(key.getBytes())
        )), null));
    }
}
