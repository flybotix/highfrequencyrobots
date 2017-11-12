package com.flybotix.hfr.codex;

import java.nio.ByteBuffer;

import com.flybotix.hfr.util.lang.EnumUtils;

public class CodexMetadata <E extends Enum<E>> {

  private int mId = 0;
  private int mCodexTypeId = 0;
  private long mTimestamp = 0;
  private Integer mKey = -1;
  
  public String toString() {
    return mId + "\t" + mCodexTypeId + "\t" + mTimestamp + "\t" + mKey;
  }
  
  public CodexMetadata(Class<E> pType, int pId, long pTimestamp) {
	  this(pType, pId, pTimestamp, -1);
  }
  
  /**
   * @param pType Enum that backs the codex
   * @param pId Initial instance id.  This will get incremented upon reset().
   * @param pTimestamp Initial timestamp, in nanoseconds.
   * @param pCompositeKey Allows for an un-changing identifier so that an enumeration may represent multiple counts of the same object type.
   */
  public CodexMetadata(Class<E> pType, int pId, long pTimestamp, Integer pCompositeKey) {
    mCodexTypeId = EnumUtils.hashOf(pType);
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
    return Integer.BYTES + Long.BYTES; 
  }
  
  public byte[] encode() {
    return ByteBuffer.allocate(sizeOf()).putInt(mId).putLong(mTimestamp).array();
  }
  
  public static <E extends Enum<E>> CodexMetadata<E> parse(Class<E> pEnum, ByteBuffer pData) {
    return new CodexMetadata<E>(pEnum, pData.getInt(), pData.getLong());
  }

  public static <E extends Enum<E>> CodexMetadata<E> empty(Class<E> pEnum) {
    return new CodexMetadata<>(pEnum, 0, 0);
  }
}
