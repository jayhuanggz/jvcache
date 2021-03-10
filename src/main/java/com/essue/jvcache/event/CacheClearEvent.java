package com.essue.jvcache.event;

public class CacheClearEvent implements CacheEvent {

  private String name;

  public CacheClearEvent(String name) {
    this.name = name;
  }

  CacheClearEvent() {}

  public String getName() {
    return name;
  }
}
