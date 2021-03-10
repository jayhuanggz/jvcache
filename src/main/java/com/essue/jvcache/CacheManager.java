package com.essue.jvcache;

public interface CacheManager {

  Cache getCache(String name);

  void init();

  void destroy();

  void clear();
}
