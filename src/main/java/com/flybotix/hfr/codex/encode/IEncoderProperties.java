package com.flybotix.hfr.codex.encode;

import java.nio.ByteBuffer;

public interface IEncoderProperties<V> {
  public Class<V> getCodexType();
  public V getDefaultValue(boolean pIsCompressedAlgorithm);
  public V[] generateEmptyArray(int pSize, boolean pIsCompressedAlgorithm);
  public int sizeOfSingle();
  public V decodeSingle(ByteBuffer pData);
  public void encodeSingle(ByteBuffer pData, V pValue);
}
