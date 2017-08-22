package com.flybotix.hfr.codex;

public interface CodexOf<T> {
  
  public default boolean usesCompression() {
    return true;
  }
  
}