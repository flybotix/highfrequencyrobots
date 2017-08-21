package com.flybotix.hfr.io.encode;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexMetadata;
import com.flybotix.hfr.io.receiver.IMessageParser;

public abstract class AbstractEncoder <E extends Enum<E>, V>{

  private final Class<E> mEnumClass;
  protected final EnumSet<E> mEnums;
  protected final int mLength;
  protected final int mBitSetByteLength;
  
  public AbstractEncoder(Class<E> pEnum) {
    mEnumClass = pEnum;
    mEnums = EnumSet.allOf(pEnum);
    mLength = mEnums.size();
    mBitSetByteLength = (mLength + Byte.SIZE-1) / Byte.SIZE;
  }

  public EnumSet<E> getEnums() {
    return mEnums;
  }
  
  public Class<E> getEnum() {
    return mEnumClass;
  }
  
  public abstract int getBufferSizeInBytes();
  public abstract V getDefaultValue();
  public abstract V[] generateEmptyArray();
  
  public byte[] encode(Codex<E, V> pData) {
    byte[] body = encodeImpl(pData);
    byte[] header = pData.meta().encode();
    
    return ByteBuffer.allocate(header.length + body.length).put(header).put(body).array();
  }
  
  public Codex<E, V> decode(ByteBuffer pData) {
    CodexMetadata<E> header = CodexMetadata.parse(mEnumClass, pData);
    Codex<E,V> body = decodeImpl(pData);
    body.setMetadata(header);
    return body;
  }
  
  protected abstract Codex<E, V> decodeImpl(ByteBuffer pData);
  
  protected abstract byte[] encodeImpl(Codex<E, V> pData);
}
