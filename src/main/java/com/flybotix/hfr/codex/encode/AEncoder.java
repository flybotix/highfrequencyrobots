package com.flybotix.hfr.codex.encode;

import java.nio.ByteBuffer;
import java.util.EnumSet;

import com.flybotix.hfr.codex.CodexMetadata;
import com.flybotix.hfr.codex.Type;
import com.flybotix.hfr.codex.Codex;

public abstract class AEncoder <V, E extends Enum<E> & Type<V>>{

  private final Class<E> mEnumClass;
  protected final EnumSet<E> mEnums;
  protected final int mLength;
  protected final int mBitSetByteLength;
  
  public AEncoder(Class<E> pEnum) {
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
  
  public byte[] encode(Codex<V, E> pData) {
    byte[] body = encodeImpl(pData);
    byte[] header = pData.meta().encode();
    
    return ByteBuffer.allocate(header.length + body.length).put(header).put(body).array();
  }
  
  public Codex<V,E> decode(ByteBuffer pData) {
    CodexMetadata<E> header = CodexMetadata.parse(mEnumClass, pData);
    Codex<V, E> body = decodeImpl(pData);
    body.setMetadata(header);
    return body;
  }
  
  protected abstract Codex<V, E> decodeImpl(ByteBuffer pData);
  
  protected abstract byte[] encodeImpl(Codex<V, E> pData);
}
