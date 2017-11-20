package com.flybotix.hfr.codex;

public class ThreadsafeCodex<V, E extends Enum<E> & CodexOf<V>> extends Codex<V, E> {
  
  private final Object mLock = new Object();

//  public ThreadsafeCodex(AEncoder<V, E> pEncoder) {
//    super(pEncoder);
//  }

  public ThreadsafeCodex(V pDefaultValue, CodexMetadata<E> pMeta) {
    super(pDefaultValue, pMeta);
  }
  
  public ThreadsafeCodex(V pDefaultValue, Class<E> pEnum) {
    super(pEnum);
  }
  
  public ThreadsafeCodex(Class<E> pEnum) {
    super(pEnum);
  }
  
  public void reset() {
    synchronized(mLock) {
      super.reset();
    }
  }
  
//  public byte[] encode() {
//    byte[] result = null;
//    synchronized(mLock) {
//      result = super.encode();
//    }
//    return result;
//  }
  
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
