package com.essue.jvcache.event;

public interface CacheEventVisitor {

  void cachePut(CachePutEvent event);

  void cacheEvict(CacheEvictEvent event);

  void cacheEvictMulti(CacheEvictMultiEvent event);

  void cacheClear(CacheClearEvent event);
}
