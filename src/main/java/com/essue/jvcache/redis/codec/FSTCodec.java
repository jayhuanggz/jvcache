package com.essue.jvcache.redis.codec;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class FSTCodec implements RedisCodec {

  private FSTConfiguration factory;

  public FSTCodec() {
    this.factory = FSTConfiguration.createDefaultConfiguration();
  }

  @Override
  public String serialize(Object value) {
    return Base64.getEncoder().encodeToString(serializeBytes(value));
  }

  @Override
  public Object deserialize(String data) {

    return this.deserializeBytes(Base64.getDecoder().decode(data));
  }

  @Override
  public byte[] serializeBytes(Object value) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
    FSTObjectOutput out = factory.getObjectOutput(stream);

    try {
      out.writeObject(value);
      out.flush();
      out.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return stream.toByteArray();
  }

  @Override
  public Object deserializeBytes(byte[] data) {

    if (data == null) {
      return null;
    }
    FSTObjectInput in = factory.getObjectInput(new ByteArrayInputStream(data));

    try {
      return in.readObject();
    } catch (ClassNotFoundException | IOException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        in.reset();

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
