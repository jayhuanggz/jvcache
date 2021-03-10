package com.essue.jvcache.ehcache.serialize;

import com.essue.jvcache.io.ByteArrayOutputStreamBuilder;
import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FstSerializer implements Serializer<Object> {

  private final FSTConfiguration factory;

  public FstSerializer() {
    this.factory = FSTConfiguration.createDefaultConfiguration();
  }

  @Override
  public ByteBuffer serialize(Object o) throws SerializerException {

    ByteArrayOutputStream stream = ByteArrayOutputStreamBuilder.get();

    FSTObjectOutput out = factory.getObjectOutput(stream);

    try {
      out.writeObject(o);
      out.flush();
      out.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return ByteBuffer.wrap(stream.toByteArray());
  }

  @Override
  public Object read(ByteBuffer byteBuffer) throws SerializerException {

    final byte[] bytes = new byte[byteBuffer.limit() - byteBuffer.position()];

    byteBuffer.get(bytes);

    FSTObjectInput in = factory.getObjectInput(new ByteArrayInputStream(bytes));

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

  @Override
  public boolean equals(Object o, ByteBuffer byteBuffer)
      throws ClassNotFoundException, SerializerException {
    return this.serialize(o).equals(byteBuffer);
  }
}
