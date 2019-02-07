package com.flybotix.hfr.codex;

import com.flybotix.hfr.util.lang.EnumUtils;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class DoubleCodexProperty <E extends Enum<E> & CodexOf<Double>> extends Codex<Double,E>{
  
  private final SimpleDoubleProperty[] mProperties;

  public DoubleCodexProperty(Class<E> pEnum) {
    super(pEnum);
    mProperties = new SimpleDoubleProperty[EnumUtils.getLength(pEnum)];
  }

  public void set(E pData, Double pValue) {
    super.set(pData, pValue);
    mProperties[pData.ordinal()].set(pValue);
  }

  public void set(int pOrdinal, Double pValue) {
    super.set(pOrdinal, pValue);
    mProperties[pOrdinal].set(pValue);
  }
  
  public void bind(E pData, ObservableValue<Double> pObservable) {
    mProperties[pData.ordinal()].bind(pObservable);
  }
  
  public void bindBiDirectional(E pData, Property<Number> pProperty) {
    mProperties[pData.ordinal()].bindBidirectional(pProperty);
  }
  
  public void addListener(E pData, ChangeListener<? super Number> pListener) {
    mProperties[pData.ordinal()].addListener(pListener);
  }
}
