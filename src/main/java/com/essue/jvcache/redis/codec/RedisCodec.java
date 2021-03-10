package com.essue.jvcache.redis.codec;

public interface RedisCodec {

  String serialize(Object value);

  Object deserialize(String data);

  byte[] serializeBytes(Object value);

  Object deserializeBytes(byte[] data);
}
