package com.flybotix.hfr.codex.encode;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;

public class BitEncoder <E extends Enum<E> & CodexOf<Boolean>> extends AEncoder<Boolean, E>{

  public BitEncoder(Class<E> pEnum) {
    super(pEnum);
  }
  
  @Override
  public int getBufferSizeInBytes() {
    return mBitSetByteLength;
  }

  @Override
  public Boolean getDefaultValue() {
    return false;
  }

  @Override
  public Boolean[] generateEmptyArray() {
    Boolean[] result = new Boolean[mLength];
    Arrays.fill(result, false);
    return result;
  }

  @Override
  protected Codex<Boolean, E> decodeImpl(ByteBuffer pData) {
    // reference BitSet.toByteArray() for this calculation
    byte[] bsarray = new byte[getBufferSizeInBytes()];
    pData.get(bsarray);
    BitSet hash = BitSet.valueOf(bsarray);
    hash.set(mLength);
    Codex<Boolean, E> result = new Codex<>(this);
    for(int i = 0; i < mLength; i++) {
      result.put(i, hash.get(i));
    }
    return result;
  }

  @Override
  protected byte[] encodeImpl(Codex<Boolean, E> pData) {
    BitSet result = new BitSet(pData.length());
    for(int i = 0; i < pData.length(); i++) {
      if(pData.get(i)) {
        result.set(i);
      }
    }
    return result.toByteArray();
  }

}
