package com.flybotix.hfr.util.lang;

import java.util.Objects;

public class NullSafe {
  /**
   * Replaces some boilerplate
   * @param target the object to test
   * @param replacementIfNull what to return if the object is null
   * @return a non-null object
   * @param <T> The type of object that will be tested and returned
   */
  public static <T> T replaceNull(T target, T replacementIfNull) { 
    return Objects.isNull(target) ? replacementIfNull : target;
  }
}
