package com.flybotix.hfr.cache;

import com.flybotix.hfr.codex.CodexOf;

/**
 * Represents a single element of Codex data at a single point in time.
 * @param <V>
 * @param <E>
 */
public class CodexElementInstance <V, E extends Enum<E> & CodexOf<V>>{
  public final V value;
  public final double time;
  public final E element;
  public CodexElementInstance(double pTime, V pValue, E pElement) {
    time = pTime;
    value = pValue;
    element = pElement;
  }
}
