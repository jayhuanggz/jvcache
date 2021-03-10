package com.essue.jvcache.event;

import rx.Observable;

public interface CacheEventBroadcastOperations {

  Observable<Void> fireCachePut(CachePutEvent event);

  Observable<Void> fireCacheEvict(CacheEvictEvent event);

  Observable<Void> fireCacheEvictMulti(CacheEvictMultiEvent event);

  Observable<Void> fireCacheClear(CacheClearEvent event);
}
