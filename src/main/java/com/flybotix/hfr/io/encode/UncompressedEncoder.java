package com.flybotix.hfr.io.encode;

import java.nio.ByteBuffer;

import com.flybotix.hfr.codex.Codex;

public class UncompressedEncoder <E extends Enum<E>, V> extends DefaultEncoder<E, V> {

  public UncompressedEncoder(Class<E> pEnum, IEncoderProperties<V> pProps) {
    super(pEnum, pProps);
  }

  @Override
  protected boolean isCompressed() {
    return false;
  }

  @Override
  protected Codex<E, V> decodeImpl(ByteBuffer pData) {
    Codex<E, V> result = new Codex<>(this);
    for(int dataidx = 0; dataidx < mLength; dataidx++) {
      result.put(dataidx, mProps.decodeSingle(pData));
    }
    return result;
  }

  @Override
  protected byte[] encodeImpl(Codex<E, V> pData) {
    ByteBuffer bb = ByteBuffer.allocate(mProps.sizeOfSingle() * mLength);
    for(int dataidx = 0; dataidx < mLength; dataidx++) {
      mProps.encodeSingle(bb, pData.get(dataidx));
    }
    return  bb.array();
  }

}