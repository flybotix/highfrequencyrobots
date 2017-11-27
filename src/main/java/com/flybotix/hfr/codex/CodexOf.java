package com.flybotix.hfr.codex;

import java.util.concurrent.TimeUnit;

import com.flybotix.hfr.util.lang.IConverter;

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
  
  public default String getTimestampShortString() {
    switch(getTimestampUnit()) {
    case SECONDS: return "(s)";
    case NANOSECONDS: return "(ns)";
    case MILLISECONDS: return "(ms)";
    case MICROSECONDS: return "(us)";
    case MINUTES: return "(min)";
    case HOURS: return "(hr)";
    case DAYS: return "days";
    }
    return getTimestampUnit().toString();
  }
}