package com.essue.jvcache.ehcache;

import org.ehcache.impl.events.CacheEventAdapter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Controls the maximum local cache hits */
public class LimitHitsStrategy extends CacheEventAdapter<String, Object> {

  private final ConcurrentHashMap<String, AtomicInteger> hits = new ConcurrentHashMap<>();

  private int maxHits = 15;

  public void setMaxHits(int maxHits) {
    this.maxHits = maxHits;
  }

  private AtomicInteger getCurrentHit(String key) {

    AtomicInteger hit = hits.get(key);

    if (hit == null) {
      hit = new AtomicInteger(0);
      AtomicInteger current = hits.putIfAbsent(key, hit);
      if (current != null) {
        hit = current;
      }
    }

    return hit;
  }

  public boolean onHit(String key) {
    AtomicInteger currentHit = getCurrentHit(key);

    boolean valid = currentHit.incrementAndGet() <= this.maxHits;
    if (!valid) {
      this.reset(key);
    }

    return valid;
  }

  public void reset(String key) {
    if (getCurrentHit(key).get() > 0) {
      hits.put(key, new AtomicInteger(0));
    }
  }

  public void clear() {
    this.hits.clear();
  }

  @Override
  protected void onRemoval(String key, Object removedValue) {
    hits.remove(key);
  }

  @Override
  protected void onExpiry(String key, Object expiredValue) {
    hits.remove(key);
  }

  @Override
  protected void onEviction(String key, Object evictedValue) {
    hits.remove(key);
  }

  @Override
  protected void onCreation(String key, Object newValue) {
    hits.put(key, new AtomicInteger(0));
  }

  @Override
  protected void onUpdate(String key, Object oldValue, Object newValue) {
    if (getCurrentHit(key).get() > 0) {
      hits.put(key, new AtomicInteger(0));
    }
  }
}
