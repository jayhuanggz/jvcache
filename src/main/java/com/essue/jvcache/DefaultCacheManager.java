package com.essue.jvcache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultCacheManager implements CacheManager {

  private CacheProvider cacheProvider;

  private Map<String, Integer> timeouts = new HashMap<>();

  private Integer defaultTimeout = 60 * 30;

  private ConcurrentHashMap<String, Cache> caches = new ConcurrentHashMap<>();

  public DefaultCacheManager(CacheProvider cacheProvider) {
    this.cacheProvider = cacheProvider;
  }

  public void setDefaultTimeout(Integer defaultTimeout) {
    this.defaultTimeout = defaultTimeout;
  }

  public void setTimeouts(Map<String, Integer> timeouts) {
    this.timeouts = timeouts;
  }

  @Override
  public Cache getCache(String name) {

    Cache cache = caches.get(name);
    if (cache == null) {
      synchronized (this) {
        cache = caches.get(name);
        if (cache == null) {
          Integer timeout = timeouts.get(name);
          if (timeout == null) {
            timeout = defaultTimeout;
          }
          cache = cacheProvider.getCache(name, timeout);
          caches.put(name, cache);
        }
      }
    }

    return cache;
  }

  @Override
  public void init() {
    cacheProvider.init();
  }

  @Override
  public void destroy() {
    cacheProvider.destroy();
  }

  @Override
  public void clear() {

    if (!caches.isEmpty()) {
      for (Cache cache : caches.values()) {
        cache.clear();
      }
    }
  }
}
