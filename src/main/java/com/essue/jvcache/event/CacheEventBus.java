package com.essue.jvcache.event;

import rx.Observable;

public interface CacheEventBus {

  Observable<Void> fire(CacheEvent event);

  <E extends CacheEvent> Observable<E> subscribe(Class<E> eventType);

  <E extends CacheEvent> Observable<Void> unsubscribe(Class<E> eventType);

  Observable<Void> destroy();
}
