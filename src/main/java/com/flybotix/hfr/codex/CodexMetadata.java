package com.flybotix.hfr.codex;

import java.nio.ByteBuffer;
import java.time.Instant;

public class CodexMetadata <E extends Enum<E>> {

  private int mId = 0;
  private int mCodexTypeId = 0;
  private long mTimestamp = 0;
  
  public CodexMetadata(Class<E> pType, int pId, long pTimestamp) {
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
