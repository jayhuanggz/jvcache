package com.essue.jvcache.composite;

import com.essue.jvcache.Cache;
import com.essue.jvcache.CacheProvider;

public class CompositeCacheProvider implements CacheProvider {

  private final CacheProvider localCacheProvider;
  private final CacheProvider sharedCacheProvider;

  public CompositeCacheProvider(
      CacheProvider localCacheProvider, CacheProvider sharedCacheProvider) {
    this.localCacheProvider = localCacheProvider;
    this.sharedCacheProvider = sharedCacheProvider;
  }

  @Override
  public Cache getCache(String name, int timeout) {

    Cache localCache = localCacheProvider.getCache(name, timeout);
    Cache sharedCache = sharedCacheProvider.getCache(name, timeout);
    return new CompositeCache(localCache, sharedCache);
  }

  @Override
  public void init() {
    localCacheProvider.init();
    sharedCacheProvider.init();
  }

  @Override
  public void destroy() {
    localCacheProvider.destroy();
    sharedCacheProvider.destroy();
  }
}
