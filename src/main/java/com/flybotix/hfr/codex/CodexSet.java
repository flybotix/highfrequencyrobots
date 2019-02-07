package com.flybotix.hfr.codex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.flybotix.hfr.util.lang.EnumUtils;
import com.sun.javafx.collections.ObservableListWrapper;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * A codex set represents one or more linked Codex objects.  Data is set
 * using a codex metadata's key() parameter.  This class provides a few
 * helper methods to quickly set a value to all linked codexes.
 * 
 * @param <V> The type backing the codex
 * @param <E> The enumeration backing the codex
 */
public class CodexSet <V, E extends Enum<E> & CodexOf<V>> {
  
  private final Map<E, ObservableList<V>> mData = new HashMap<>();
  
  public CodexSet(Class<E> pEnum) {
    for(E e : EnumUtils.getEnums(pEnum)) {
      mData.put(e, FXCollections.observableArrayList());
    }
  }
  
  public void add(Codex<V,E> pCodex) {
    for(E e : mData.keySet()) {
      mData.get(e).add(pCodex.get(e));
    }
  }
  
  public void addListener(E pEnum, ListChangeListener<V> pListener) {
    mData.get(pEnum).addListener(pListener);
  }
  
  public void removeListener(E pEnum, ListChangeListener<V> pListener) {
    mData.get(pEnum).removeListener(pListener);
  }
  
}
