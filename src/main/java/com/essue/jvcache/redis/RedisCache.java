package com.essue.jvcache.redis;

import com.essue.jvcache.event.*;
import com.essue.jvcache.redis.codec.RedisCodec;
import com.essue.jvcache.shared.SharedCache;
import io.vertx.redis.RedisClient;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;
import rx.Observable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RedisCache implements SharedCache {

  private String name;
  private RedisClient client;
  private RedisCodec redisCodec;
  private int timeout;
  private CacheEventBus cacheEventBus;

  public RedisCache(
      CacheEventBus cacheEventBus,
      String name,
      RedisClient client,
      RedisCodec redisCodec,
      int timeout) {
    this.name = name;
    this.client = client;
    this.redisCodec = redisCodec;
    this.timeout = timeout;
    this.cacheEventBus = cacheEventBus;
  }

  @Override
  public Observable<Object> get(String key) {
    ObservableFuture<String> future = RxHelper.observableFuture();
    Observable result =
        future.flatMap(
            data -> {
              if (data == null) {
                return Observable.just(null);
              }

              CacheValue deserialized = (CacheValue) redisCodec.deserialize(data);

              if (deserialized.getExpireTime() <= System.currentTimeMillis()) {

                ObservableFuture<Long> del = RxHelper.observableFuture();
                Observable<CacheValue> map =
                    del.map(
                        val -> {
                          return null;
                        });
                client.hdel(name, key, del.toHandler());

                return map;
              }
              return Observable.just(deserialized.getValue());
            });
    client.hget(name, key, future.toHandler());
    return result;
  }

  @Override
  public Observable<Void> put(String key, Object value) {

    ObservableFuture<Long> future = RxHelper.observableFuture();

    CacheValue cacheValue =
        value == null ? null : new CacheValue(value, timeout * 1000 + System.currentTimeMillis());

    Observable<Void> result =
        future.flatMap(
            aLong -> {
              return this.fireCachePut(new CachePutEvent(name, key));
            });

    client.hset(name, key, redisCodec.serialize(cacheValue), future.toHandler());

    return result;
  }

  @Override
  public Observable<Void> evict(String key) {

    ObservableFuture<Long> future = RxHelper.observableFuture();
    Observable<Void> result =
        future.flatMap(
            aLong -> {
              return this.fireCacheEvict(new CacheEvictEvent(name, key));
            });

    client.hdel(name, key, future.toHandler());
    return result;
  }

  @Override
  public Observable<Void> evict(Collection<String> keys) {
    ObservableFuture<Long> future = RxHelper.observableFuture();
    Observable<Void> result =
        future.flatMap(
            val -> {
              return this.fireCacheEvictMulti(new CacheEvictMultiEvent(name, keys));
            });

    List<String> cacheKeys = new ArrayList<>(keys);

    client.hdelMany(name, cacheKeys, future.toHandler());
    return result;
  }

  @Override
  public Observable<Void> clear() {

    ObservableFuture<Long> future = RxHelper.observableFuture();
    Observable<Void> result =
        future.flatMap(
            array -> {
              return this.fireCacheClear(new CacheClearEvent(name));
            });
    client.del(name, future.toHandler());
    return result;
  }

  private static final class CacheValue implements Serializable {
    private Object value;
    private long expireTime;

    public CacheValue(Object value, long expireTime) {
      this.value = value;
      this.expireTime = expireTime;
    }

    CacheValue() {}

    public Object getValue() {
      return value;
    }

    public void setValue(Object value) {
      this.value = value;
    }

    public long getExpireTime() {
      return expireTime;
    }

    public void setExpireTime(long expireTime) {
      this.expireTime = expireTime;
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Observable<Void> fireCachePut(CachePutEvent event) {
    return cacheEventBus.fire(event);
  }

  @Override
  public Observable<Void> fireCacheEvict(CacheEvictEvent event) {
    return cacheEventBus.fire(event);
  }

  @Override
  public Observable<Void> fireCacheEvictMulti(CacheEvictMultiEvent event) {
    return cacheEventBus.fire(event);
  }

  @Override
  public Observable<Void> fireCacheClear(CacheClearEvent event) {
    return cacheEventBus.fire(event);
  }
}
