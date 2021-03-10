package com.essue.jvcache.event;

public class CachePutEvent implements CacheEvent {

  private String name;
  private String key;

  public CachePutEvent(String name, String key) {
    this.name = name;
    this.key = key;
  }

  CachePutEvent() {}

  public String getName() {
    return name;
  }

  public String getKey() {
    return key;
  }
}
