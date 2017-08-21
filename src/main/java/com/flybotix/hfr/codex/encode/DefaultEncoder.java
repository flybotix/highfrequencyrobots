package com.flybotix.hfr.codex.encode;

import com.flybotix.hfr.codex.CodexMetadata;

public abstract class DefaultEncoder <E extends Enum<E>, V> extends AEncoder<E, V> {

  protected final IEncoderProperties<V> mProps;
  
  public DefaultEncoder(Class<E> pEnum, IEncoderProperties<V> pProps) {
    super(pEnum);
    mProps = pProps;
  }
  
  protected abstract boolean isCompressed();

  @Override
  public int getBufferSizeInBytes() {
    return mLength * mProps.sizeOfSingle() + (isCompressed() ? mBitSetByteLength : 0) + CodexMetadata.sizeOf();
  }

  @Override
  public V getDefaultValue() {
    return mProps.getDefaultValue(isCompressed());
  }

  @Override
  public V[] generateEmptyArray() {
    return mProps.generateEmptyArray(mLength, isCompressed());
  }
}
