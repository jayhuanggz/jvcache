package com.essue.jvcache.ehcache;

import com.essue.jvcache.event.CacheClearEvent;
import com.essue.jvcache.event.CacheEvictEvent;
import com.essue.jvcache.event.CacheEvictMultiEvent;
import com.essue.jvcache.event.CachePutEvent;
import com.essue.jvcache.local.LocalCache;
import org.ehcache.Cache;
import rx.Observable;

import java.util.Collection;
import java.util.HashSet;

public class EhCacheCache implements LocalCache {
  private String name;
  private Cache cache;

  public EhCacheCache(String name, Cache cache) {
    this.name = name;
    this.cache = cache;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Observable<Object> get(String key) {

    return Observable.just(cache.get(key));
  }

  @Override
  public Observable<Void> put(String key, Object value) {

    cache.put(key, value);

    return Observable.just(null);
  }

  @Override
  public Observable<Void> evict(String key) {
    cache.remove(key);
    return Observable.just(null);
  }

  @Override
  public Observable<Void> evict(Collection<String> keys) {
    cache.removeAll(new HashSet<>(keys));
    return Observable.just(null);
  }

  @Override
  public Observable<Void> clear() {
    cache.clear();
    return Observable.just(null);
  }

  @Override
  public void onCachePut(CachePutEvent event) {
    cache.remove(event.getKey());
  }

  @Override
  public void onCacheEvict(CacheEvictEvent event) {
    cache.remove(event.getKey());
  }

  @Override
  public void onCacheEvictMulti(CacheEvictMultiEvent event) {
    cache.removeAll(new HashSet<>(event.getKeys()));
  }

  @Override
  public void onCacheClear(CacheClearEvent event) {
    cache.clear();
  }
}
