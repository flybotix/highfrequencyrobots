package com.flybotix.hfr.codex;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A codex set represents one or more linked Codex objects.  Data is set
 * using a codex metadata's key() parameter.  This class provides a few
 * helper methods to quickly set a value to all linked codexes.
 * 
 * @param <V> The type backing the codex
 * @param <E> The enumeration backing the codex
 */
public class CodexSet <V, E extends Enum<E> & CodexOf<V>>{
  private final Codex<V,E>[] mCodexes;
  
  public CodexSet(Codex<V,E>[] pCodexes) {
    mCodexes = pCodexes;
  }
  
  public void setAll(E pData, V pValue) {
    for(int i = 0; i < mCodexes.length; i++) {
      mCodexes[i].set(pData, pValue);
    }
  }
  
  public boolean setByIndex(int pIndex, E pData, V pValue) {
    boolean result = false;
    if(pIndex >= 0 && pIndex < mCodexes.length) {
      mCodexes[i].set(pData, pValue);
    }
    return result;
  }
  
  public boolean setByKey(Integer pKey, E pData, V pValue) {
    boolean result = false;
    Codex<V, E> codex = find(pKey);
    if(codex != null) {
      codex.set(pData, pValue);
      result = true;
    }
    return result;
  }
  
  private Codex<V,E> find(Integer pKey) {
    Codex<V,E> result =
      Arrays.stream(mCodexes)
        .filter(c -> Objects.equals(c.meta().key(), pKey))
        .collect(Collectors.toList())
        .iterator()
        .next();
    
//    for(int i = 0; i < mCodexes.length; i++) {
//      if(mCodexes[i].meta().key() == pKey) {
//        result = mCodexes[i];
//      }
//    }
    return result;
  }
}
