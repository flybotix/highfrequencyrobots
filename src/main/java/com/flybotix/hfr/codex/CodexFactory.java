package com.flybotix.hfr.codex;

import com.flybotix.hfr.ETestData;
import com.flybotix.hfr.codex.encode.AEncoder;
import com.flybotix.hfr.codex.encode.EncoderFactory;

public final class CodexFactory {
  
  /**
   * ONLY USE THIS FOR GLOBAL DATA
   * @param pEnum
   * @return
   */
  public <E extends Enum<E> & Type<Double>> Codex<Double, E> doubles(Class<E> pEnum) {
    AEncoder<Double, E> ae = EncoderFactory.getDoubleEncoder(pEnum, true); 
    return new Codex<Double, E>(ae);
  }
  
  public static <E extends Enum<E>> void test(Class<E> pEnum) {
  }
  
  static CodexFactory inst() {
    return INST;
  }
  private static final CodexFactory INST = new CodexFactory();
  
  private CodexFactory() {
    
  }
  
  private static enum ENONTEST {
    A,B,C
  }
  
  public static void main(String[] pArgs) {
    CodexFactory.test(ETestData.class);
    CodexFactory.test(ENONTEST.class);
  }
}
