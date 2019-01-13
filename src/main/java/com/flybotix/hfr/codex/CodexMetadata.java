package com.flybotix.hfr.codex;

import java.nio.ByteBuffer;

import com.flybotix.hfr.util.lang.EnumUtils;

/**
 * Class which holds a bit of metadata about a codex instance.
 * @param <E>
 */
public class CodexMetadata <E extends Enum<E>> {

  private int mId = 0;
  private int mCodexTypeId = 0;
  private double mTimestamp = 0;
  private Integer mKey = -1;
  private final Class<E> mEnum;
  private static ICodexTimeProvider sTIME_PROVIDER = new ICodexTimeProvider() {
  };
  
  public String toString() {
    return mId + "\t" + mCodexTypeId + "\t" + mTimestamp + "\t" + mKey;
  }
  
  /**
   * @param pType Enum that backs the codex
   * @param pId Initial instance id.  This will get incremented upon reset().
   * @param pTimestamp the timestamp with units matching E.getTimestampUnit (default = seconds).
   */
  public CodexMetadata(Class<E> pType, int pId, double pTimestamp) {
	  this(pType, pId, pTimestamp, -1);
  }
  
  /**
   * @param pType Enum that backs the codex
   * @param pId Initial instance id.  This will get incremented upon reset().
   * @param pTimestamp the timestamp with units matching E.getTimestampUnit (default = seconds).
   * @param pCompositeKey Allows for an un-changing identifier so that an enumeration may represent multiple counts of the same object type.
   */
  public CodexMetadata(Class<E> pType, int pId, double pTimestamp, Integer pCompositeKey) {
    mCodexTypeId = EnumUtils.hashOf(pType);
    mEnum = pType;
    mId = pId;
    mKey = pCompositeKey;
    mTimestamp = pTimestamp;
  }

  public static void overrideTimeProvider(ICodexTimeProvider pTimeProvider) {
    sTIME_PROVIDER = pTimeProvider;
  }
  
  public Class<E> getEnum() {
    return mEnum;
  }
  
  /**
   * Incrememnts the instance ID, but does not set the current time.
   */
  public void next() {
    next(false);
  }
  
  /**
   * Increments the instance ID and sets the nanotimestamp to current time 
   * @param pUpdateTime whether or not to set the nanotime to current time
   */
  public void next(boolean pUpdateTime) {
    mId++;
    if(pUpdateTime) {
      setTimestamp(sTIME_PROVIDER.getTimestamp());
    }
  }
  
  /**
   * Provides a mechanism to override ID and Time.  This is useful for associating multiple
   * different codex types to the same ID in order to compare data across many types.
   * @param pId ID to set
   * @param pTime Time to override
   */
  public void override(int pId, double pTime) {
	  mId = pId;
	  setTimestamp(pTime);
  }
  
  /**
   * @return the current cycle ID of the Codex.  This also matches the
   * number of times Codex.reset() has been called for this instance. 
   */
  public int id() {
    return mId;
  }
  
  /*package*/ void overrideId(Integer id) {
    mId = id;
  }
  
  /**
   * This is set by calling EnumUtils.hashOf() with the enum class.  In theory this
   * matches the potential message id, but that isn't the case for batched messages.
   * @return a hash representation of the backing enum
   */
  public int type() {
    return mCodexTypeId;
  }
  
  /**
   * @return the timestamp with units matching E.getTimestampUnit (default = seconds).
   */
  public double timestamp() {
    return mTimestamp;
  }
  
  /**
   * Manually set the current nano timestamp. the caller
   * has full control over whether it is a system-relative time
   * or whether it is nanoseconds relative to some specific point in
   * time (such as match start).  So technically a coder could choose
   * milliseconds here, although that defeats the purpose of one-codex-per-cycle.
   * @param pTimestamp the timestamp with units matching E.getTimestampUnit (default = seconds).
   */
  public void setTimestamp(double pTimestamp) {
    mTimestamp = pTimestamp;
  }
  
  /**
   * When used with the id() method, this helps create a composite unique identifier
   * to an exact cycle of a codex instance.
   * @return a user-set key that represents an instance ID
   */
  public Integer key() {
	  return mKey;
  }
  
  /**
   * @return the transmitted size of the metadata
   */
  public static int sizeOf() {
    return Integer.BYTES + Double.BYTES + Integer.BYTES; 
  }
  
  /**
   * @return a byte array that can be decoded via CodexMetadata.parse()
   */
  public byte[] encode() {
    return ByteBuffer.allocate(sizeOf()).putInt(mId).putDouble(mTimestamp).putInt(mKey).array();
  }
  
  public void setCompositeKey(int pKey) {
    mKey = pKey;
  }
  
  /**
   * @param pEnum The enum that backs the Codex.
   * @param pData The byte[] array wrapped in a buffer.  Hopefully it was created with the encode() method.
   * @return a Codex Meta data object, supposing nothing went wrong
   */
  public static <E extends Enum<E>> CodexMetadata<E> parse(Class<E> pEnum, ByteBuffer pData) {
    return new CodexMetadata<E>(pEnum, pData.getInt(), pData.getDouble(), pData.getInt());
  }

  /**
   * Creates an empty metadata object based off the num.
   * @param pEnum The enumeration that backs the Codex
   * @return a Codex Metadata object that is initialzed with everything = 0
   */
  public static <E extends Enum<E>> CodexMetadata<E> empty(Class<E> pEnum) {
    return new CodexMetadata<>(pEnum, 0, 0);
  }
}
