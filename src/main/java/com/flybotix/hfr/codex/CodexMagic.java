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
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public final class CodexMagic {
  private static final ILog mLog = Logger.createLog(CodexMagic.class);
  
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

  public <V, E extends Enum<E> & CodexOf<V>> void registerEnum(Class<E> pEnum) {
    Class<V> valueClass = getTypeOfCodex(pEnum);
    IEncoderProperties<V> props = findPropertiesForClass(valueClass);
    registerEnum(pEnum, props);
  }

  public <V, E extends Enum<E> & CodexOf<V>> void registerEnum(Class<E> pEnum, IEncoderProperties<V> pProperties) {
    if(pProperties == null) {
      throw new IllegalArgumentException("Cannot create encoders & codexes when the EncoderProperties<V> parameter is null.");
    }
    
    DefaultEncoders<V, E> def = new DefaultEncoders<>(pEnum, pProperties);
    mDefaultEncoders.put(pEnum, def);
  }
  
  @SuppressWarnings("unchecked")
  public <T, E extends Enum<E> & CodexOf<T>> AEncoder<T, E> of(Class<E> pEnum, boolean pUseCompression) {
    AEncoder<T, E> result = null;
    if(!mDefaultEncoders.containsKey(pEnum)) {
      registerEnum(pEnum);
    }
    DefaultEncoders<T, E> encs = (DefaultEncoders<T, E>) mDefaultEncoders.get(pEnum);
    if(pUseCompression) {
      result = encs.compressed;
    } else {
      result = encs.uncompressed;
    }
    return result;
  }
  
  public static <E extends Enum<E> & CodexOf<Boolean>> AEncoder<Boolean, E> getBooleanEncoder(Class<E> pEnum) {
    return new BitEncoder<E>(pEnum);
  }
  
  public <T, E extends Enum<E> & CodexOf<T>> void registerProperties(Class<E> pEnum, IEncoderProperties<T> pProperties) {
    mProperties.add(pProperties);
    mDefaultEncoders.put(pEnum, new DefaultEncoders<>(pEnum, pProperties));
  }
  

  private <T, E extends Enum<E> & CodexOf<T>> IEncoderProperties<T> getPropertiesForEnum(Class<E> pEnum) {
    return findPropertiesForClass(getTypeOfCodex(pEnum));
  }
  
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

  private Set<IEncoderProperties<?>> mProperties = new HashSet<>();
  
  @SuppressWarnings("unchecked")
  private static <V, E extends Enum<E> & CodexOf<V>> Class<V> getTypeOfCodex(Class<E> pEnum) {
    Class<CodexOf<V>> forcecast = (Class<CodexOf<V>>)pEnum;
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
  }
  private static class Holder {
    private static CodexMagic instance = new CodexMagic();
  }
  
  private Map<Class<? extends Enum<?>>, DefaultEncoders<?,?>> mDefaultEncoders = new HashMap<>();

  
  /**************************************************
   * Properties
   **************************************************/
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
