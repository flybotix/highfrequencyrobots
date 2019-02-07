package com.flybotix.hfr.codex;

import java.util.Arrays;
import java.util.EnumSet;

import com.flybotix.hfr.codex.encode.IEncoderProperties;
import com.flybotix.hfr.util.lang.EnumUtils;
import com.flybotix.hfr.util.lang.IConverter;

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
  protected final Class<V> mType;
  
  public static final CodexMagic of = CodexMagic.inst();
  public static final CodexMagic encoder = CodexMagic.inst();
  
  /**
   * Creates a new Codex with the set metadata and default value
   * @param pDefaultValue
   * @param pMeta
   */
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

  /**
   * Creates a Codex with the set default value and a blank metadata object
   * @param pDefaultValue Default value to set.  May be null
   * @param pEnum Enumeration backing the codex.  May NOT be null.
   */
  public Codex(V pDefaultValue, Class<E> pEnum) {
    this(pDefaultValue, CodexMetadata.empty(pEnum));
  }
  
  /**
   * Creates a Codex with <code>null</code> as the default value
   * @param pEnum Enumeration backing the codex.  May NOT be null.
   */
  public Codex(Class<E> pEnum) {
    this(null, pEnum);
  }
  
  /**
   * Takes the value from the E2 codex and inserts it into the 'ToField'.
   * @param pOtherCodex The codex to pull the data from
   * @param pFromField The field in the other codex to get the data from
   * @param pToField The field in this codex to put the data into.
   */
  public <E2 extends Enum<E2> & CodexOf<V>> void map(Codex<V, E2> pOtherCodex, E2 pFromField, E pToField) {
    set(pToField, pOtherCodex.get(pFromField));
  }
  
  /**
   * @return the metadata
   */
  public CodexMetadata<E> meta() {
    return mMeta;
  }
  
  /**
   * Overrides the metadata
   * @param pMeta The new metadata
   */
  public void setMetadata(CodexMetadata<E> pMeta) {
    mMeta = pMeta;
  }
  
  /**
   * @return (effectively) this is E.values().length
   */
  public int length() {
    return mData.length;
  }
  
  public String getCSVHeader() {
    EnumSet<E> set = EnumSet.allOf(meta().getEnum());
    String tu = set.iterator().next().getTimestampShortString();
    StringBuilder sb = new StringBuilder();
    sb.append("Codex Name").append(',');
    sb.append("Key").append(',');
    sb.append("Id").append(',');
    sb.append("Time ").append(tu).append(',');
    for(E e : set) {
      sb.append(e.toString().replaceAll("_", " ")).append(',');
    }
    return sb.toString();
  }
  
  /**
   * @return A CSV string that represents this instance of the Codex, including metadata
   */
  public String toCSV() {
    return toCSV(from -> from == null ? "" : from.toString());
  }
  
  /**
   * @param pToString A custom converter to print a single element of an object
   * @return A CSV string that represents this instance of the Codex, including metadata
   */
  public String toCSV(IConverter<V, String> pToString) {
    StringBuilder sb = new StringBuilder();
    sb.append(meta().getEnum().getSimpleName()).append(',');
    sb.append(meta().key()).append(',');
    sb.append(meta().id()).append(',');
    sb.append(meta().timestamp()).append(',');
    for(int i = 0; i < mData.length; i++) {
      sb.append(pToString.convert(mData[i])).append(',');
    }
    return sb.toString();
  }
  
  /**
   * @param pCSV String to parse
   * @param pParser Converter to from a String to the type represented by <code>V</code>
   * @return <code>this</code> if successful.  Allows for stream mapping.
   */
  public Codex<V,E> fillFromCSV(String pCSV, IConverter<String, V> pParser) {
    String[] elements = pCSV.split(",");
    if(!elements[0].equalsIgnoreCase(meta().getEnum().getSimpleName())) {
      return this;
    }
    meta().setCompositeKey(Integer.parseInt(elements[1]));
    meta().overrideId(Integer.parseInt(elements[2]));
    meta().setTimestamp(Double.parseDouble(elements[3]));
    
    for(int i = 0; i < length(); i++) {
      set(i, pParser.convert(elements[i+4]));
    }
    
    return this;
  }
  
  /**
   * Resets the data to the default value (which is usually <code>null</code>
   */
  public void reset() {
    Arrays.fill(mData, mDefaultValue);
    mMeta.next(true);
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

  /**
   * @return the class representing the type of the data
   */
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
