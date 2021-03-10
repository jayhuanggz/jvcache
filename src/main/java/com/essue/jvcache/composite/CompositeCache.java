package com.essue.jvcache.composite;

import com.essue.jvcache.Cache;
import rx.Observable;

import java.util.Collection;

public class CompositeCache implements Cache {

  private Cache localCache;
  private Cache sharedCache;

  public CompositeCache(Cache localCache, Cache sharedCache) {
    this.localCache = localCache;
    this.sharedCache = sharedCache;
  }

  @Override
  public String getName() {
    return localCache.getName();
  }

  @Override
  public Observable<Object> get(String key) {

    return localCache
        .get(key)
        .flatMap(
            local -> {
              if (local == null) {

                return sharedCache
                    .get(key)
                    .flatMap(
                        shared -> {
                          if (shared == null) {
                            return Observable.just(null);
                          } else {
                            return localCache
                                .put(key, shared)
                                .map(
                                    aVoid -> {
                                      return shared;
                                    });
                          }
                        });

              } else {
                return Observable.just(local);
              }
            });
  }

  @Override
  public Observable<Void> put(String key, Object value) {

    return localCache
        .evict(key)
        .mergeWith(sharedCache.put(key, value))
        .mergeWith(localCache.put(key, value));
  }

  @Override
  public Observable<Void> evict(String key) {
    return localCache.evict(key).mergeWith(sharedCache.evict(key));
  }

  @Override
  public Observable<Void> evict(Collection<String> keys) {
    return localCache.evict(keys).mergeWith(sharedCache.evict(keys));
  }

  @Override
  public Observable<Void> clear() {
    return localCache.clear().mergeWith(sharedCache.clear());
  }
}
