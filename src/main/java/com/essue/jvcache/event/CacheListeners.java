package com.essue.jvcache.event;

public interface CacheListeners {

  void onCachePut(CachePutEvent event);

  void onCacheEvict(CacheEvictEvent event);

  void onCacheEvictMulti(CacheEvictMultiEvent event);

  void onCacheClear(CacheClearEvent event);
}
