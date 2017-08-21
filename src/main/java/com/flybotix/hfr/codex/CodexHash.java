package com.flybotix.hfr.codex;

import java.util.BitSet;

public class CodexHash{
  BitSet bs = new BitSet();
  int nonNullCount = 0;
  
  public BitSet getBitSet() {
    return bs;
  }
  
  public int getNonNullCount() {
    return nonNullCount;
  }
}