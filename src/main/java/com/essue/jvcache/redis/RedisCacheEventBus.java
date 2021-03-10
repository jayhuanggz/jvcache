package com.essue.jvcache.redis;

import com.essue.jvcache.event.CacheEvent;
import com.essue.jvcache.event.CacheEventBus;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.ObservableHandler;
import io.vertx.rx.java.RxHelper;
import io.vertx.rxjava.redis.RedisClient;
import rx.Observable;

import java.util.*;

public class RedisCacheEventBus implements CacheEventBus {

  private RedisClient redisClient;

  private Map<Class<CacheEvent>, String> channels;

  private Set<String> subscribedChannels = new HashSet<>();

  private Map<String, MessageConsumer> consumers = new HashMap<>();

  private Vertx vertx;

  public RedisCacheEventBus(Vertx vertx, RedisClient redisClient) {
    this.redisClient = redisClient;
    this.vertx = vertx;
  }

  public void setChannels(Map<Class<CacheEvent>, String> channels) {
    this.channels = channels;
  }

  @Override
  public Observable<Void> fire(CacheEvent event) {

    String channel = channels.get(event.getClass());

    if (channel == null) {
      throw new IllegalArgumentException("No channel found for CacheEvent: " + event.getClass());
    }

    ObservableFuture<Long> future = RxHelper.observableFuture();
    Observable<Void> result =
        future.map(
            count -> {
              return null;
            });
    redisClient.publish(channel, Json.encode(event), future.toHandler());

    return result;
  }

  @Override
  public <E extends CacheEvent> Observable<E> subscribe(Class<E> eventType) {

    String channel = channels.get(eventType);

    if (channel == null) {
      throw new IllegalArgumentException("No channel found for CacheEvent: " + eventType);
    }

    if (!subscribedChannels.add(channel)) {
      throw new IllegalArgumentException("Channel " + channel + " has been subscribed!");
    }

    redisClient.subscribeObservable(channel);
    ObservableHandler<Message<JsonObject>> handler = RxHelper.observableHandler(true);

    Observable<E> result =
        handler.flatMap(
            received -> {
              String message = received.body().getJsonObject("value").getString("message");

              E event = Json.decodeValue(message, eventType);
              return Observable.just(event);
            });

    consumers.put(
        channel,
        vertx.eventBus().<JsonObject>consumer("io.vertx.redis." + channel, handler.toHandler()));
    return result;
  }

  @Override
  public <E extends CacheEvent> Observable<Void> unsubscribe(Class<E> eventType) {

    String channel = channels.get(eventType);

    if (channel == null) {
      throw new IllegalArgumentException("No channel found for CacheEvent: " + eventType);
    }

    if (subscribedChannels.remove(channel)) {
      MessageConsumer consumer = consumers.remove(channel);
      if (consumer != null) {
        consumer.unregister();
      }
      return redisClient.unsubscribeObservable(Arrays.asList(channel));
    }

    return Observable.just(null);
  }

  @Override
  public Observable<Void> destroy() {
    if (subscribedChannels.isEmpty()) {
      return Observable.just(null);
    }

    return redisClient
        .unsubscribeObservable(new ArrayList<>(this.subscribedChannels))
        .mergeWith(redisClient.closeObservable());
  }
}
