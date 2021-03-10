package com.essue.jvcache;

public interface CacheProvider {

  Cache getCache(String name, int timeout);

  void init();

  void destroy();
}
