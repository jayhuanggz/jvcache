package com.essue.jvcache.ehcache;

import com.essue.jvcache.Cache;
import com.essue.jvcache.CacheProvider;
import com.essue.jvcache.event.*;
import com.essue.jvcache.local.LocalCache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.event.EventFiring;
import org.ehcache.event.EventOrdering;
import org.ehcache.event.EventType;
import org.ehcache.spi.serialization.Serializer;
import org.ehcache.xml.XmlConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class EhCacheCacheProvider implements CacheProvider {

  private CacheEventBus eventBus;
  private CacheManager ehCacheManager;

  private XmlConfiguration defaultCacheConfiguration;

  private Map<String, LocalCache> caches = new HashMap<>();

  private Serializer serializer;

  public EhCacheCacheProvider(
      CacheEventBus eventBus,
      CacheManager ehCacheManager,
      XmlConfiguration defaultCacheConfiguration) {
    this.eventBus = eventBus;
    this.ehCacheManager = ehCacheManager;
    this.defaultCacheConfiguration = defaultCacheConfiguration;
  }

  public void setSerializer(Serializer serializer) {
    this.serializer = serializer;
  }

  @Override
  public Cache getCache(String name, int timeout) {

    org.ehcache.Cache<String, Object> cache =
        ehCacheManager.getCache(name, String.class, Object.class);

    if (cache == null) {

      try {
        CacheConfigurationBuilder<String, Object> config =
            defaultCacheConfiguration.newCacheConfigurationBuilderFromTemplate(
                "DEFAULT", String.class, Object.class);

        if (serializer != null) {
          config = config.withValueSerializer(serializer);
          config = config.withValueSerializingCopier();
        }

        cache = ehCacheManager.createCache(name, config);

      } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    LimitHitsStrategy strategy = new LimitHitsStrategy();
    cache
        .getRuntimeConfiguration()
        .registerCacheEventListener(
            strategy,
            EventOrdering.ORDERED,
            EventFiring.SYNCHRONOUS,
            new HashSet(Arrays.asList(EventType.values())));
    EhCacheCache result = new EhCacheCache(name, cache);

    LimitedHitsCache cacheHitsCache = new LimitedHitsCache(result, strategy);
    caches.put(name, cacheHitsCache);
    return cacheHitsCache;
  }

  @Override
  public void init() {

    eventBus
        .subscribe(CachePutEvent.class)
        .subscribe(
            event -> {
              LocalCache cache = caches.get(event.getName());
              if (cache != null) {
                cache.onCachePut(event);
              }
            });

    eventBus
        .subscribe(CacheEvictEvent.class)
        .subscribe(
            event -> {
              LocalCache cache = caches.get(event.getName());
              if (cache != null) {
                cache.onCacheEvict(event);
              }
            });

    eventBus
        .subscribe(CacheEvictMultiEvent.class)
        .subscribe(
            event -> {
              LocalCache cache = caches.get(event.getName());
              if (cache != null) {
                cache.onCacheEvictMulti(event);
              }
            });

    eventBus
        .subscribe(CacheClearEvent.class)
        .subscribe(
            event -> {
              LocalCache cache = caches.get(event.getName());
              if (cache != null) {
                cache.onCacheClear(event);
              }
            });
  }

  @Override
  public void destroy() {
    eventBus.unsubscribe(CacheEvictEvent.class);
    eventBus.unsubscribe(CacheClearEvent.class);
    eventBus.unsubscribe(CachePutEvent.class);
    eventBus.unsubscribe(CacheEvictMultiEvent.class);
  }
}
