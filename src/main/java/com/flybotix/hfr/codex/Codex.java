package com.flybotix.hfr.codex;

import java.util.Arrays;

import com.flybotix.hfr.codex.encode.IEncoderProperties;
import com.flybotix.hfr.util.lang.EnumUtils;

/**
 * It's like an enum map, but with less safety and better performance.
 * 
 * Note - Codex and its methods are NOT thread safe.  DO NOT (e.g.) call
 * reset() and encode() on the same codex instance from multiple threads.
 * You will eventually get weird buffer errors, and your code may explode.
 * 
 * @param <V> The type backing the codex
 * @param <E> The enumeration backing the codex
 */
public class Codex <V, E extends Enum<E> & CodexOf<V>>{
  
  protected CodexMetadata<E> mMeta;
  protected V[] mData;
  protected V mDefaultValue = null;
  protected Class<V> mType;
  
  public static final CodexMagic of = CodexMagic.inst();
  public static final CodexMagic encoder = CodexMagic.inst();
  
  public Codex(V pDefaultValue, CodexMetadata<E> pMeta) {
    mMeta = pMeta;
    // A bit complicated ... but effectively we need a helper to know what to cast `V` to since we're using an array.
    // We could do what ArrayList does and just use an array of objects.  TODO
    IEncoderProperties<V> props = of.getPropertiesForEnum(mMeta.getEnum());
    if(props == null) {
      mData = (V[])new Object[EnumUtils.getLength(pMeta.getEnum())]; // Maybe this doesn't work? who knows.
      mDefaultValue = pDefaultValue;
    } else {
      if(pDefaultValue == null) {
        mDefaultValue = props.getDefaultValue(true);
      } else {
        mDefaultValue = pDefaultValue;
      }
      mData = props.generateEmptyArray(EnumUtils.getLength(mMeta.getEnum()), true);
    }
    Arrays.fill(mData, mDefaultValue);
    mType = CodexMagic.getTypeOfCodex(pMeta.getEnum());
  }
  
  public Codex(V pDefaultValue, Class<E> pEnum) {
    this(pDefaultValue, CodexMetadata.empty(pEnum));
  }
  
  public Codex(Class<E> pEnum) {
    this(null, pEnum);
  }
  
  /**
   * @return the metadata
   */
  public CodexMetadata<E> meta() {
    return mMeta;
  }
  
  public void setMetadata(CodexMetadata<E> pMeta) {
    mMeta = pMeta;
  }
  
  /**
   * @return (effectively) this is E.values().length
   */
  public int length() {
    return mData.length;
  }
  
//  public void reset() {
//    Arrays.fill(mData, mEncoder.getDefaultValue());
//    mMeta.next();
//  }
  
  public void reset() {
    Arrays.fill(mData, mDefaultValue);
    mMeta.next();
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

  public Class<V> type() {
    return mType;
  }
  
  /**
   * Useful for looping functionss
   * @return the value in the array at the ordinal
   * @param pOrdinal The index/ordinal to get
   */
  public V get(int pOrdinal) {
    return mData[pOrdinal];
  }
  
  /**
   * @return the value in the array at the location of the enum's ordinal
   * @param pData The data piece to get
   */
  public V get(E pData) {
    return get(pData.ordinal());
  }
  
  /**
   * Set some data.
   * @param pData The data to set
   * @param pValue The value of the data
   */
  public void set(E pData, V pValue) {
    set(pData.ordinal(), pValue);
  }

  /**
   * Set some data via a cached (or looped) index/ordinal
   * @param pOrdinal The index of the data (matches E.ordinal())
   * @param pValue The value of the data
   */
  public void set(int pOrdinal, V pValue) {
    mData[pOrdinal] = pValue;
  }

  /**
   * This seemingly-useless method helps reduce boilerplate.  It also adds a way to hack booleans/flags into a codex of non-booleans.
   * @param pOrdinal Value to check
   * @return whether the value at the enum's location is not null and does not equal the codex's default value.
   */
  public boolean isSet(int pOrdinal) {
    return !isNull(pOrdinal);
  }


  /**
   * This seemingly-useless method helps reduce boilerplate.  It also adds a way to hack booleans/flags into a codex of non-booleans.
   * @param pOrdinal Value to check
   * @return whether the value at the enum's location is null or equals the codex's default value.
   */
  public boolean isNull(int pOrdinal) {
    return mData[pOrdinal] == null || mData[pOrdinal].equals(mDefaultValue);
  }

  /**
   * This seemingly-useless method helps reduce boilerplate.  It also adds a way to hack booleans/flags into a codex of non-booleans.
   * @param pEnum Value to check
   * @return whether the value at the enum's location is not null and does not equal the codex's default value.
   */
  public boolean isSet(E pEnum) {
    return isSet(pEnum.ordinal());
  }


  /**
   * This seemingly-useless method helps reduce boilerplate.  It also adds a way to hack booleans/flags into a codex of non-booleans.
   * @param pEnum Value to check
   * @return whether the value at the enum's location is null or equals the codex's default value.
   */
  public boolean isNull(E pEnum) {
	  return isNull(pEnum.ordinal());
  }
  
  /**
   * @return a hash based upon set values.
   */
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
