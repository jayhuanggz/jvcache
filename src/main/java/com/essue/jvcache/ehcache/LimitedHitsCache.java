package com.essue.jvcache.ehcache;

import com.essue.jvcache.event.CacheClearEvent;
import com.essue.jvcache.event.CacheEvictEvent;
import com.essue.jvcache.event.CacheEvictMultiEvent;
import com.essue.jvcache.event.CachePutEvent;
import com.essue.jvcache.local.LocalCache;
import rx.Observable;

import java.util.Collection;

public class LimitedHitsCache implements LocalCache {

  private LocalCache delegate;
  private LimitHitsStrategy strategy;

  public LimitedHitsCache(LocalCache delegate, LimitHitsStrategy strategy) {
    this.delegate = delegate;
    this.strategy = strategy;
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public Observable<Object> get(String key) {

    return delegate
        .get(key)
        .map(
            cached -> {
              if (cached == null) {
                strategy.reset(key);
              } else if (!strategy.onHit(key)) {
                delegate.evict(key);
                cached = null;
              }

              return cached;
            });
  }

  @Override
  public Observable<Void> put(String key, Object value) {
    return delegate.put(key, value);
  }

  @Override
  public Observable<Void> evict(String key) {
    return delegate.evict(key);
  }

  @Override
  public Observable<Void> evict(Collection<String> keys) {
    return delegate.evict(keys);
  }

  @Override
  public Observable<Void> clear() {
    strategy.clear();
    return delegate.clear();
  }

  @Override
  public void onCachePut(CachePutEvent event) {

    delegate.onCachePut(event);
  }

  @Override
  public void onCacheEvict(CacheEvictEvent event) {
    delegate.onCacheEvict(event);
  }

  @Override
  public void onCacheEvictMulti(CacheEvictMultiEvent event) {
    delegate.onCacheEvictMulti(event);
  }

  @Override
  public void onCacheClear(CacheClearEvent event) {
    strategy.clear();
    delegate.onCacheClear(event);
  }
}
