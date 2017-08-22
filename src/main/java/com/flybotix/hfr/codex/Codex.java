package com.flybotix.hfr.codex;

import java.util.Arrays;

import com.flybotix.hfr.codex.encode.AEncoder;

/**
 * It's like an enum map, but with less safety and better performance.
 */
public class Codex <V, E extends Enum<E> & CodexOf<V>>{
  
  private CodexMetadata<E> mMeta;
  private final AEncoder<V, E> mEncoder;
  private V[] mData;
  
  public static final CodexFactory of = CodexFactory.inst();
  
  public Codex(AEncoder<V, E> pEncoder) {
    this(pEncoder, CodexMetadata.empty(pEncoder.getEnum()));
  }
  
  public Codex(AEncoder<V, E> pEncoder, CodexMetadata<E> pMeta) {
    mData = pEncoder.generateEmptyArray();
    mMeta = pMeta;
    mEncoder = pEncoder;
  }
  
  public CodexMetadata<E> meta() {
    return mMeta;
  }
  
  public byte[] encode() {
    return mEncoder.encode(this);
  }

  public void setMetadata(CodexMetadata<E> pMeta) {
    mMeta = pMeta;
  }
  
  public int length() {
    return mData.length;
  }
  
  public void reset() {
    Arrays.fill(mData, mEncoder.getDefaultValue());
  }
  
  public String toString() {
    if(mData == null) return "null";
    return Arrays.toString(mData);
  }

  @SuppressWarnings("unchecked")
  public boolean equals(Object pOther) {
    Codex<V, E> o = (Codex<V, E>)pOther;
    return Arrays.equals(o.mData, mData);
  }
  
  public V get(int pOrdinal) {
    return mData[pOrdinal];
  }
  
  public V get(E pData) {
    return get(pData.ordinal());
  }
  
  public void put(E pData, V pValue) {
    put(pData.ordinal(), pValue);
  }
  
  public void put(int pOrdinal, V pValue) {
    mData[pOrdinal] = pValue;
  }
  
  public CodexHash hash() {
    CodexHash codex = new CodexHash();
    for(int i = 0; i < mData.length; i++) {
      if(get(i) != null) {
        codex.bs.set(i);
        codex.nonNullCount++;
      }
    }
    
    // Set this bit so the BitSet length matches the enumeration length
    codex.bs.set(mData.length);
    return codex;
  }
  
}
