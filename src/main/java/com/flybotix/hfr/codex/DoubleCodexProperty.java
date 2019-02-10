package com.flybotix.hfr.codex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.flybotix.hfr.util.lang.EnumUtils;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * @author JesseK
 *
 *  A class which adds thread-safe listeners to a Codex.  This class extends Codex of doubles.
 *
 * @param <E>
 */
public class DoubleCodexProperty <E extends Enum<E> & CodexOf<Double>> extends Codex<Double,E>{
  
  private final SimpleDoubleProperty[] mProperties;
  private final SimpleBooleanProperty[] mIsSetProperties;
  private final Map<E, List<ChangeListener<? super Number>>> mChangeListeners = new HashMap<>();
  private final Executor mThreadPool;

  public DoubleCodexProperty(Class<E> pEnum) {
    this(pEnum, Executors.newFixedThreadPool(1));
  }
  
  public DoubleCodexProperty(Class<E> pEnum, Executor pThreadPool) {
    super(pEnum);
    mProperties = new SimpleDoubleProperty[EnumUtils.getLength(pEnum)];
    mIsSetProperties = new SimpleBooleanProperty[EnumUtils.getLength(pEnum)];
    mThreadPool = pThreadPool;
    for(E e : EnumUtils.getEnums(pEnum)) {
      mChangeListeners.put(e, new ArrayList<>());
    }
  }

  public void set(E pData, Double pValue) {
    super.set(pData, pValue);
    Platform.runLater(() -> {
      mProperties[pData.ordinal()].set(pValue);
      mIsSetProperties[pData.ordinal()].set(isSet(pData));
    });
    mThreadPool.execute(() -> {
      for(ChangeListener<? super Number> listener : mChangeListeners.get(pData)) {
        listener.changed(null, null, pValue);
      }
    });
  }

  public void set(int pOrdinal, Double pValue) {
    Platform.runLater(() -> {
      mProperties[pOrdinal].set(pValue);
      mIsSetProperties[pOrdinal].set(isSet(pOrdinal));
    });
    mThreadPool.execute(() -> {
      E e = EnumUtils.getEnums(mMeta.getEnum(), true).get(pOrdinal);
      for(ChangeListener<? super Number> listener : mChangeListeners.get(e)) {
        listener.changed(null, null, pValue);
      }
    });
  }
  
  public void bind(E pData, ObservableValue<Double> pObservable) {
    mProperties[pData.ordinal()].bind(pObservable);
  }
  
  public void bindBiDirectional(E pData, Property<Number> pProperty) {
    mProperties[pData.ordinal()].bindBidirectional(pProperty);
  }
  
  public void addListener(E pData, ChangeListener<? super Number> pListener) {
    mProperties[pData.ordinal()].addListener(pListener);
    mChangeListeners.get(pData).add(pListener);
  }
  
  public void bindToSetProperty(E pData, ObservableValue<Boolean> pObservable) {
    mIsSetProperties[pData.ordinal()].bind(pObservable);
  }
  
  public void bindBiDirectionalToSetProperty(E pData, Property<Boolean> pProperty) {
    mIsSetProperties[pData.ordinal()].bindBidirectional(pProperty);
  }
  
  public void addListenerToSetProperty(E pData, ChangeListener<Boolean> pListener) {
    mIsSetProperties[pData.ordinal()].addListener(pListener);
  }
}
