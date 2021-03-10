package com.essue.jvcache.event;

public class CacheEvictEvent implements CacheEvent {

  private String name;
  private String key;

  public CacheEvictEvent(String name, String key) {
    this.name = name;
    this.key = key;
  }

  CacheEvictEvent() {}

  public String getName() {
    return name;
  }

  public String getKey() {
    return key;
  }
}
