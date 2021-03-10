package com.essue.jvcache.redis;

import com.essue.jvcache.Cache;
import com.essue.jvcache.CacheProvider;
import com.essue.jvcache.event.CacheEventBus;
import com.essue.jvcache.redis.codec.RedisCodec;
import io.vertx.redis.RedisClient;

public class RedisCacheProvider implements CacheProvider {

  private CacheEventBus eventBus;
  private RedisClient client;
  private RedisCodec codec;

  public RedisCacheProvider(CacheEventBus eventBus, RedisClient client, RedisCodec codec) {
    this.eventBus = eventBus;
    this.client = client;
    this.codec = codec;
  }

  @Override
  public Cache getCache(String name, int timeout) {

    RedisCache cache = new RedisCache(eventBus, name, client, codec, timeout);

    return cache;
  }

  public void init() {}

  public void destroy() {}
}
