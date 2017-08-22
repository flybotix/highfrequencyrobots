package com.flybotix.hfr.codex.encode;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.flybotix.hfr.codex.ICodexType;

public class EncoderFactory {
  public static <E extends Enum<E> & ICodexType<Double>> AEncoder<Double, E> getDoubleEncoder(Class<E> pEnum, boolean pUseCompression) {
    if(pUseCompression) {
      return new CompressedEncoder<Double, E>(pEnum, DOUBLE_ENCODER_PROPERTIES);
    } else {
      return new UncompressedEncoder<Double, E>(pEnum, DOUBLE_ENCODER_PROPERTIES);
    }
  }
  
  public static <E extends Enum<E> & ICodexType<Long>> AEncoder<Long, E> getLongEncoder(Class<E> pEnum, boolean pUseCompression) {
    if(pUseCompression) {
      return new CompressedEncoder<Long, E>(pEnum, LONG_ENCODER_PROPERTIES);
    } else {
      return new UncompressedEncoder<>(pEnum, LONG_ENCODER_PROPERTIES);
    }
  }
  
  public static <E extends Enum<E> & ICodexType<Boolean>> AEncoder<Boolean, E> getBooleanEncoder(Class<E> pEnum) {
    return new BitEncoder<E>(pEnum);
  }


  /**************************************************
   * Properties
   **************************************************/
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
}
