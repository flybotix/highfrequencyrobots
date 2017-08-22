package com.flybotix.hfr.codex;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.flybotix.hfr.ETestData;
import com.flybotix.hfr.codex.encode.AEncoder;
import com.flybotix.hfr.codex.encode.BitEncoder;
import com.flybotix.hfr.codex.encode.CompressedEncoder;
import com.flybotix.hfr.codex.encode.IEncoderProperties;
import com.flybotix.hfr.codex.encode.UncompressedEncoder;

public final class CodexFactory {
  
  /**
   * ONLY USE THIS FOR GLOBAL DATA
   * @param pEnum
   * @return
   */
  public <E extends Enum<E> & CodexOf<Double>> Codex<Double, E> doubles(Class<E> pEnum) {
    AEncoder<Double, E> ae = getDoubleEncoder(pEnum, true); 
    return new Codex<Double, E>(ae);
  }
  
  public <V, E extends Enum<E> & CodexOf<V>> Codex<V, E> any(Class<E> pEnum) {
    Class<V> valueClass = getTypeOfCodex(pEnum);
    IEncoderProperties<V> props = getPropertiesForClass(valueClass);
    if(props == null) {
      throw new IllegalArgumentException("Unable to find a properties implementation associated to " + valueClass + ". " +
        " If it is primitive, notify the developer.  If it is a custom type, make one yourself and register it.");
    }
    AEncoder<V, E> enc = new CompressedEncoder<>(pEnum, props);
    return new Codex<V, E>(enc);
  }
  
  public static <V, E extends Enum<E> & CodexOf<V>> Class<V> getTypeOfCodex(Class<E> pEnum) {
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
  
  public static <E extends Enum<E> & CodexOf<Double>> AEncoder<Double, E> getDoubleEncoder(Class<E> pEnum, boolean pUseCompression) {
    if(pUseCompression) {
      return new CompressedEncoder<Double, E>(pEnum, DOUBLE_ENCODER_PROPERTIES);
    } else {
      return new UncompressedEncoder<Double, E>(pEnum, DOUBLE_ENCODER_PROPERTIES);
    }
  }
  
  public static <E extends Enum<E> & CodexOf<Long>> AEncoder<Long, E> getLongEncoder(Class<E> pEnum, boolean pUseCompression) {
    if(pUseCompression) {
      return new CompressedEncoder<Long, E>(pEnum, LONG_ENCODER_PROPERTIES);
    } else {
      return new UncompressedEncoder<>(pEnum, LONG_ENCODER_PROPERTIES);
    }
  }
  
  public static <E extends Enum<E> & CodexOf<Boolean>> AEncoder<Boolean, E> getBooleanEncoder(Class<E> pEnum) {
    return new BitEncoder<E>(pEnum);
  }
  
  

  
  /**************************************************
   * Properties
   **************************************************/
  
  @SuppressWarnings("unchecked")
  private <T> IEncoderProperties<T> getPropertiesForClass(Class<T> pClass) {
    IEncoderProperties<T> result = null;
    for(EncoderPropertiesMap<?> encmap : mProperties) {
      if(encmap.equals(pClass)) {
        result = (IEncoderProperties<T>) encmap.mProps;
        break;
      }
    }
    return result;
  }

  private Set<EncoderPropertiesMap<?>> mProperties = new HashSet<>();
  
  private static class EncoderPropertiesMap<T> {
    final Class<T> mClass;
    final IEncoderProperties<T> mProps;
    EncoderPropertiesMap(Class<T> pClass, IEncoderProperties<T> pProps) {
      mClass = pClass;
      mProps = pProps;
    }
    boolean equals(Class<?> pClass) {
      return mClass.equals(pClass);
    }
    public int hashCode() {
      return mClass.hashCode();
    }
  }
  private static final IEncoderProperties<Long> LONG_ENCODER_PROPERTIES = new IEncoderProperties<Long>() {
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

  
  
  static CodexFactory inst() {
    return INST;
  }
  private static final CodexFactory INST = new CodexFactory();
  
  private CodexFactory() {
    mProperties.add(new EncoderPropertiesMap<>(Double.class, DOUBLE_ENCODER_PROPERTIES));
    mProperties.add(new EncoderPropertiesMap<>(Long.class, LONG_ENCODER_PROPERTIES));
  }
}
