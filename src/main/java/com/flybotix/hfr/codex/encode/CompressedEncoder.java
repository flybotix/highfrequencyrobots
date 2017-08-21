package com.flybotix.hfr.codex.encode;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexHash;
import com.flybotix.hfr.codex.CodexMetadata;

public class CompressedEncoder <E extends Enum<E>, V> extends DefaultEncoder<E, V> {

  public CompressedEncoder(Class<E> pEnum, IEncoderProperties<V> pProps) {
    super(pEnum, pProps);
  }

  @Override
  protected boolean isCompressed() {
    return true;
  }

  @Override
  protected Codex<E, V> decodeImpl(ByteBuffer pData) {
    int length = pData.remaining();
    int initPos = pData.position();

    // reference BitSet.toByteArray() for this calculation
    byte[] bsarray = new byte[mBitSetByteLength];
    pData.get(bsarray);
    BitSet hash = BitSet.valueOf(bsarray);
    hash.set(mLength);

    // Usually the offset is 0, but with UDP connections it's non-zero.
    pData.position(bsarray.length + initPos /*+ pData.arrayOffset()*/);
    V[] decoded = mProps.generateEmptyArray((length - bsarray.length) / mProps.sizeOfSingle(), false);
    for(int i = 0; i < decoded.length; i++) {
      decoded[i] = mProps.decodeSingle(pData);
    }

    Codex<E, V> result = new Codex<E, V>(this);
    int dataidx = 0;
    for(int e = 0; e < mLength; e++) {
      if(hash.get(e)) {
        result.put(e, decoded[dataidx]);
        dataidx++;
      }
    }
    return result;
  }

  @Override
  protected byte[] encodeImpl(Codex<E, V> pData) {
    CodexHash hash = pData.hash();
    byte[] bsbytes = hash.getBitSet().toByteArray();
    ByteBuffer bb = ByteBuffer.allocate(bsbytes.length + mProps.sizeOfSingle() * hash.getNonNullCount());
    bb.put(bsbytes);
    for(int e = 0; e < mLength; e++) {
      if(pData.get(e) != null) {
        mProps.encodeSingle(bb, pData.get(e));
      }
    }
    return  bb.array();
  }

}
