package com.flybotix.hfr.codex;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.flybotix.hfr.codex.encode.AEncoder;
import com.flybotix.hfr.codex.encode.BitEncoder;
import com.flybotix.hfr.codex.encode.CompressedEncoder;
import com.flybotix.hfr.codex.encode.IEncoderProperties;
import com.flybotix.hfr.codex.encode.UncompressedEncoder;
import com.flybotix.hfr.util.lang.MethodCaller;

public final class CodexMagic {

  private Set<IEncoderProperties<?>> mProperties = new HashSet<>();
  private Map<Class<? extends Enum<?>>, DefaultEncoders<?,?>> mDefaultEncoders = new HashMap<>();
  
  /**
   * Creates a codex based upon the passed-in enumeration
   * @param pEnum An enumeration that implements the CodexOf interface
   * @return A codex
   * @param <V> The type backing the codex
   * @param <E> The enumeration backing the codex
   */
  public <V, E extends Enum<E> & CodexOf<V>> Codex<V, E> thisEnum(Class<E> pEnum) {
    Class<V> valueClass = getTypeOfCodex(pEnum);
    IEncoderProperties<V> props = findPropertiesForClass(valueClass);
    if(props == null) {
      throw new IllegalArgumentException("Unable to find a properties implementation associated to " + valueClass + ". " +
        " If it is primitive, notify the developer.  If it is a custom type, make one yourself and register it.");
    }
    AEncoder<V, E> enc = new CompressedEncoder<>(pEnum, props);
    return new Codex<V, E>(enc);
  }

  /**
   * Uses the codex type of the enumeration to generate properties for the enumeration.  Advanced usage only.
   * @param pEnum The codex enumeration to register.
   * @param <V> The type backing the codex
   * @param <E> The enumeration backing the codex
   */
  public <V, E extends Enum<E> & CodexOf<V>> void registerEnum(Class<E> pEnum) {
    Class<V> valueClass = getTypeOfCodex(pEnum);
    IEncoderProperties<V> props = findPropertiesForClass(valueClass);
    registerEnum(pEnum, props);
  }

  /**
   * Internally-caches the properties for usage with the codex enumeration.  Helps to ensure
   * identical encoding behavior when using multiple instances of codexes for a single enumeration.
   * @param pEnum The codex enumeration.
   * @param pProperties Encoder Properties
   * @param <V> The type backing the codex
   * @param <E> The enumeration backing the codex
   */
  public <V, E extends Enum<E> & CodexOf<V>> void registerEnum(Class<E> pEnum, IEncoderProperties<V> pProperties) {
    if(pProperties == null) {
      throw new IllegalArgumentException("Cannot create encoders & codexes when the EncoderProperties<V> parameter is null.");
    }
    
    DefaultEncoders<V, E> def = new DefaultEncoders<>(pEnum, pProperties);
    mDefaultEncoders.put(pEnum, def);
  }

  /**
   * @param pEnum a Codex-based enumeration
   * @return an encoder for the given Enum class
   * @param <V> The type backing the codex
   * @param <E> The enumeration backing the codex
   */
  public <V, E extends Enum<E> & CodexOf<V>> AEncoder<V, E> of(Class<E> pEnum) {
    Boolean useCompression = MethodCaller.getBooleanFromEnum(pEnum, "usesCompression", true);
    return of(pEnum, useCompression);
  }
  
  /**
   * @param pEnum A Codex-based enumeration
   * @param pUseCompression Should the encoder use compression? If the data is sometimes sparse, set this to true.  Otherwise, false.
   * @return an encoder for the given Enum class
   * @param <V> The type backing the codex
   * @param <E> The enumeration backing the codex
   */
  public <V, E extends Enum<E> & CodexOf<V>> AEncoder<V, E> of(Class<E> pEnum, boolean pUseCompression) {
    AEncoder<V, E> result = null;
    if(!mDefaultEncoders.containsKey(pEnum)) {
      registerEnum(pEnum);
    }
    @SuppressWarnings("unchecked")
    DefaultEncoders<V, E> encs = (DefaultEncoders<V, E>) mDefaultEncoders.get(pEnum);
    if(pUseCompression) {
      result = encs.compressed;
    } else {
      result = encs.uncompressed;
    }
    return result;
  }
  
  /**
   * Creates an encoder for a Boolean-based Codex Enumeration.  Boolean encoders use the BitSet class rather
   * than an array of Booleans, making the entire thing very efficient to store and transmit.
   * @param pEnum An enumeration that implements CodexOf&#60;Boolean&#62;
   * @return An encoder for Boolean codexes
   * @param <E> The enumeration backing the codex
   */
  public static <E extends Enum<E> & CodexOf<Boolean>> AEncoder<Boolean, E> getBooleanEncoder(Class<E> pEnum) {
    return new BitEncoder<E>(pEnum);
  }
  
  /**
   * Advaned Usage.  Sets custom proprties for a particular enumeration.  Custom proprties can override default
   * values of a codex, alter the encode/decode algorithms, etc.  This also allows for Codexes to be used
   * with complex objects rather than just primitives.
   * @param pEnum A codex-based enumeration
   * @param pProperties Properties to set for the codex
   * @param <V> The type backing the codex
   * @param <E> The enumeration backing the codex
   */
  public <V, E extends Enum<E> & CodexOf<V>> void registerProperties(Class<E> pEnum, IEncoderProperties<V> pProperties) {
    mProperties.add(pProperties);
    mDefaultEncoders.put(pEnum, new DefaultEncoders<>(pEnum, pProperties));
  }
  

  /**
   * @param pEnum A codex-based enumeration
   * @return The registered properties based upon the Type of the enum
   * @param <V> The type backing the codex
   * @param <E> The enumeration backing the codex
   */
  private <V, E extends Enum<E> & CodexOf<V>> IEncoderProperties<V> getPropertiesForEnum(Class<E> pEnum) {
    return findPropertiesForClass(getTypeOfCodex(pEnum));
  }
  
  /**
   * Holder method for compressed & uncompressed encoders of the primitive encoders
   *
   * @param <V> Primitive Type
   * @param <E> Codex-based enumeration
   */
  class DefaultEncoders<V, E extends Enum<E> & CodexOf<V>> {
    private final AEncoder<V, E> uncompressed;
    private final AEncoder<V, E> compressed;
    
    public DefaultEncoders(Class<E> pEnum) {
      this(pEnum, getPropertiesForEnum(pEnum));
    }
    
    public DefaultEncoders(Class<E> pEnum, IEncoderProperties<V> props) {
      uncompressed = new UncompressedEncoder<>(pEnum, props);
      compressed = new CompressedEncoder<>(pEnum, props);
    }
  }
  
  /*
   * Lookup method for properties.  Returns null if they are not found.
   * @param pClass
   * @return
   */
  @SuppressWarnings("unchecked")
  private <T> IEncoderProperties<T> findPropertiesForClass(Class<T> pClass) {
    IEncoderProperties<T> result = null;
    for(IEncoderProperties<?> encmap : mProperties) {
      if(encmap.getCodexType().equals(pClass)) {
        result = (IEncoderProperties<T>) encmap;
        break;
      }
    }
    return result;
  }
  
  @SuppressWarnings("unchecked")
  private static <V, E extends Enum<E> & CodexOf<V>> Class<V> getTypeOfCodex(Class<E> pEnum) {
//    Class<CodexOf<V>> forcecast = (Class<CodexOf<V>>)pEnum;
    Class<CodexOf<V>> forcecast = Class.class.cast(pEnum);
    Type[] iface = forcecast.getGenericInterfaces();
    Class<V> resultType = null;
    for(Type t : iface) {
      if(t.toString().contains(CodexOf.class.getSimpleName())) {
        resultType = (Class<V>) ((ParameterizedType)t).getActualTypeArguments()[0];
        break;
      }
    }
    
    return resultType;
  }
  
  static CodexMagic inst() {
    return Holder.instance;
  }
  
  private CodexMagic() {
    mProperties.add(DOUBLE_ENCODER_PROPERTIES);
    mProperties.add(LONG_ENCODER_PROPERTIES);
    mProperties.add(INTEGER_ENCODER_PROPERTIES);
  }
  private static class Holder {
    private static CodexMagic instance = new CodexMagic();
  }
  
  /**************************************************
   * Properties
   **************************************************/
  private static final IEncoderProperties<Integer> INTEGER_ENCODER_PROPERTIES = new IEncoderProperties<Integer>() {
    public Class<Integer> getCodexType() {
      return Integer.class;
    }
    
    public Integer getDefaultValue(boolean pIsCompressedAlgorithm) {
      if(pIsCompressedAlgorithm)return null;
      else return Integer.MIN_VALUE;
    }

    public Integer[] generateEmptyArray(int pSize, boolean pIsCompressedAlgorithm) {
      Integer[] result = new Integer[pSize];
      Arrays.fill(result, getDefaultValue(pIsCompressedAlgorithm));
      return result;
    }

    @Override
    public int sizeOfSingle() {
      return Integer.BYTES;
    }

    @Override
    public Integer decodeSingle(ByteBuffer pData) {
      return pData.getInt();
    }

    @Override
    public void encodeSingle(ByteBuffer pData, Integer pValue) {
      pData.putInt(pValue);
    }
  };
  
  private static final IEncoderProperties<Long> LONG_ENCODER_PROPERTIES = new IEncoderProperties<Long>() {
    public Class<Long> getCodexType() {
      return Long.class;
    }
    
    public Long getDefaultValue(boolean pIsCompressedAlgorithm) {
      if(pIsCompressedAlgorithm)return null;
      else return Long.MIN_VALUE;
    }

    public Long[] generateEmptyArray(int pSize, boolean pIsCompressedAlgorithm) {
      Long[] result = new Long[pSize];
      Arrays.fill(result, getDefaultValue(pIsCompressedAlgorithm));
      return result;
    }

    @Override
    public int sizeOfSingle() {
      return Long.BYTES;
    }

    @Override
    public Long decodeSingle(ByteBuffer pData) {
      return pData.getLong();
    }

    @Override
    public void encodeSingle(ByteBuffer pData, Long pValue) {
      pData.putLong(pValue);
    }
  };
  
  private static final IEncoderProperties<Double> DOUBLE_ENCODER_PROPERTIES = new IEncoderProperties<Double>() {
    public Class<Double> getCodexType() {
      return Double.class;
    }
    
    public Double getDefaultValue(boolean pIsCompressedAlgorithm) {
      if(pIsCompressedAlgorithm)return null;
      else return Double.NaN;
    }

    public Double[] generateEmptyArray(int pSize, boolean pIsCompressedAlgorithm) {
      Double[] result = new Double[pSize];
      Arrays.fill(result, getDefaultValue(pIsCompressedAlgorithm));
      return result;
    }

    @Override
    public int sizeOfSingle() {
      return Double.BYTES;
    }

    @Override
    public Double decodeSingle(ByteBuffer pData) {
      return pData.getDouble();
    }

    @Override
    public void encodeSingle(ByteBuffer pData, Double pValue) {
      pData.putDouble(pValue);
    }
  };
}
