package dev.yewintnaing.protocol;

public sealed interface RespType
    permits RespArray,
            RespBulkString,
            RespError,
            RespInteger,
            RespNull,
            RespNullArray,
            RespNullBulkString,
            RespSimpleString {
}
