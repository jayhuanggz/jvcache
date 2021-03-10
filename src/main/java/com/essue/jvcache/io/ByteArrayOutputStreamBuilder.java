package com.essue.jvcache.io;

import java.io.ByteArrayOutputStream;

public class ByteArrayOutputStreamBuilder {

  private static final ThreadLocal<ByteArrayOutputStream> local =
      new ThreadLocal<ByteArrayOutputStream>() {

        @Override
        protected ByteArrayOutputStream initialValue() {
          return new ByteArrayOutputStream(1024);
        }
      };

  public static ByteArrayOutputStream get() {
    ByteArrayOutputStream out = local.get();
    out.reset();
    return out;
  }
}
