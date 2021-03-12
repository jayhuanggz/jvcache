jvcache is a simple vertx based library keep local cache like ehcahe, and distributed cache like redis in sync.  It has built in ehcache and redis implementations, though you can extend to support other caching framework of your choice.  It was written back in 2016. 



```

CacheEventBus cacheEeventBus = new RedisCacheEventBus(vertx, redisClient);

CacheProvider localCacheProvider = new EhCacheCacheProvider(cacheEventBus, ehcacheManager, cacheConfiguration);

CacheProvider distributedCacheProvider = new RedisCacheProvider(cacheEventBus, redisClient, redisCodec);

CacheManager cacheManager = new DefaultCacheManager(new CompositeCacheProvider(localCacheProvider, distributedCacheProvider););

Cache cache = cacheManager.getCache("");
// then use the cache as usual 

```



<b>CacheEventBus</b> - for publishing and subscribing cache event like  evict, put. The default implementation is RedisEventBus which uses the pub/sub mechanism of redis. You can use a specialised message broker by implementing <b>EventBus</b> interface

<b>CacheProvider</b> - Provider of a specific cache implementation. It has built in <b>EhCacheCacheProvider</b> and <b>RedisCacheProvider</b>, you can extend to use other cache provider by implementing this class

<b>CacheManager</b> - A top level class that wraps everything underneath and provides an api for the user to retrieving caches



