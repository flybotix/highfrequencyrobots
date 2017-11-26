package com.flybotix.hfr.codex;

import java.util.concurrent.TimeUnit;

/**
 * The backbone of the Codex Enumerations, this interface is simply a soft 'contract'
 * that helps define a Type that each enumeration represents.
 * 
 * @param <T> The type of data this CodexOf interface represents. For example, an enumeration
 * may be <code>public enum EScienceData implements CodexOf&#60;Double&#62;</code>
 */
public interface CodexOf<T> {
  
  public default boolean usesCompression() {
    return true;
  }
  
  public default TimeUnit getTimestampUnit() {
    return TimeUnit.SECONDS;
  }
  
}