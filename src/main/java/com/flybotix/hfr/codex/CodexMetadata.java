package com.flybotix.hfr.codex;

import java.nio.ByteBuffer;

public class CodexMetadata <E extends Enum<E>> {

  private int mId = 0;
  private int mCodexTypeId = 0;
  private long mTimestamp = 0;
  private Integer mKey;
  
  public CodexMetadata(Class<E> pType, int pId, long pTimestamp) {
	  this(pType, pId, pTimestamp, null);
  }
  
  /**
   * @param pType Enum that backs the codex
   * @param pId Initial instance id.  This will get incremented upon reset().
   * @param pTimestamp Initial timestamp, in nanoseconds.
   * @param pCompositeKey Allows for an un-changing identifier so that an enumeration may represent multiple counts of the same object type.
   */
  public CodexMetadata(Class<E> pType, int pId, long pTimestamp, Integer pCompositeKey) {
    mCodexTypeId = pType.hashCode();
    mId = pId;
  }
  
  public void next() {
    mId++;
    mTimestamp = System.nanoTime();
  }
  
  public int id() {
    return mId;
  }
  
  public int type() {
    return mCodexTypeId;
  }
  
  public long time() {
    return mTimestamp;
  }
  
  public Integer key() {
	  return mKey;
  }
  
  public static int sizeOf() {
    return Integer.BYTES + Integer.BYTES + Long.BYTES; 
  }
  
  public byte[] encode() {
    return ByteBuffer.allocate(sizeOf()).putInt(mId).putLong(mTimestamp).putInt(mKey).array();
  }
  
  public static <E extends Enum<E>> CodexMetadata<E> parse(Class<E> pEnum, ByteBuffer pData) {
    return new CodexMetadata<E>(pEnum, pData.getInt(), pData.getLong(), pData.getInt());
  }

  public static <E extends Enum<E>> CodexMetadata<E> empty(Class<E> pEnum) {
    return new CodexMetadata<>(pEnum, 0, 0);
  }
}
