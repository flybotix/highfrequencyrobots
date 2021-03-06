package com.flybotix.hfr.codex.encode;

import java.nio.ByteBuffer;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;

public class UncompressedEncoder <V, E extends Enum<E> & CodexOf<V>> extends DefaultEncoder<V, E> {

  public UncompressedEncoder(Class<E> pEnum, IEncoderProperties<V> pProps) {
    super(pEnum, pProps);
  }
  
  public AEncoder<V,E> createClone() {
    return new UncompressedEncoder<V,E>(getEnum(), mProps);
  }

  @Override
  protected boolean isCompressed() {
    return false;
  }

  @Override
  protected Codex<V, E> decodeImpl(ByteBuffer pData) {
//    Codex<V, E> result = new Codex<>(this);
    Codex<V, E> result = new Codex<>(getDefaultValue(), getEnum());
    for(int dataidx = 0; dataidx < mLength; dataidx++) {
      result.set(dataidx, mProps.decodeSingle(pData));
    }
    return result;
  }

  @Override
  protected byte[] encodeImpl(Codex<V, E> pData) {
    ByteBuffer bb = ByteBuffer.allocate(mProps.sizeOfSingle() * mLength);
    for(int dataidx = 0; dataidx < mLength; dataidx++) {
      mProps.encodeSingle(bb, pData.get(dataidx));
    }
    return  bb.array();
  }

}