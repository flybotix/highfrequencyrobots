package com.flybotix.hfr.codex;

import com.flybotix.hfr.codex.CodexOf;
import com.flybotix.hfr.codex.encode.AEncoder;

public class ThreadsafeCodex<V, E extends Enum<E> & CodexOf<V>> extends Codex<V, E> {
  
  private final Object mLock = new Object();

  public ThreadsafeCodex(AEncoder<V, E> pEncoder) {
    super(pEncoder);
  }
  
  public void reset() {
    synchronized(mLock) {
      super.reset();
    }
  }
  
  public byte[] encode() {
    byte[] result = null;
    synchronized(mLock) {
      result = super.encode();
    }
    return result;
  }
  
  public void set(E pData, V pValue) {
    synchronized(mLock) {
      super.set(pData, pValue);
    }
  }

  public void set(int pOrdinal, V pValue) {
    synchronized(mLock) {
      super.set(pOrdinal, pValue);
    }
  }
  public CodexHash hash() {
    CodexHash result = null;
    synchronized(mLock) {
      result = super.hash();
    }
    return result;
  }
}
