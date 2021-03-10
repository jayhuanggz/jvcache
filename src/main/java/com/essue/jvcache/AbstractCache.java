package com.essue.jvcache;

public abstract class AbstractCache implements Cache {

  protected String name;

  public AbstractCache(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }
}
