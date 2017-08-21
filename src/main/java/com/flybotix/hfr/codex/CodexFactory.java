package com.flybotix.hfr.codex;

import com.flybotix.hfr.codex.encode.AEncoder;
import com.flybotix.hfr.codex.encode.EncoderFactory;

public final class CodexFactory {
  
  /**
   * ONLY USE THIS FOR GLOBAL DATA
   * @param pEnum
   * @return
   */
  public <E extends Enum<E>> Codex<E, Double> doubles(Class<E> pEnum) {
    AEncoder<E, Double> ae = EncoderFactory.getDoubleEncoder(pEnum, true); 
    return new Codex<E, Double>(ae);
  }
  
  
  static CodexFactory inst() {
    return INST;
  }
  private static final CodexFactory INST = new CodexFactory();
  
  private CodexFactory() {
    
  }
}
