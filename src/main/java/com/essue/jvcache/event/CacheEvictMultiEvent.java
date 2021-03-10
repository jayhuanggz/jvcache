package com.essue.jvcache.event;

import java.util.Collection;

public class CacheEvictMultiEvent implements CacheEvent {
  private String name;

  private Collection<String> keys;

  public CacheEvictMultiEvent(String name, Collection<String> keys) {
    this.name = name;
    this.keys = keys;
  }

  CacheEvictMultiEvent() {}

  public String getName() {
    return name;
  }

  public Collection<String> getKeys() {
    return keys;
  }
}
