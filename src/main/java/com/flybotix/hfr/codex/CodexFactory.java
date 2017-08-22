package com.flybotix.hfr.codex;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;

import com.flybotix.hfr.ETestData;
import com.flybotix.hfr.codex.encode.AEncoder;
import com.flybotix.hfr.codex.encode.CompressedEncoder;
import com.flybotix.hfr.codex.encode.EncoderFactory;

public final class CodexFactory {
  
  /**
   * ONLY USE THIS FOR GLOBAL DATA
   * @param pEnum
   * @return
   */
  public <E extends Enum<E> & ICodexType<Double>> Codex<Double, E> doubles(Class<E> pEnum) {
    AEncoder<Double, E> ae = EncoderFactory.getDoubleEncoder(pEnum, true); 
    return new Codex<Double, E>(ae);
  }
  
  public static <V, E extends Enum<E> & ICodexType<V>> Class<V> getTypeOfCodex(Class<E> pEnum) {
    Class<ICodexType<V>> forcecast = (Class<ICodexType<V>>)pEnum;
    Type[] iface = forcecast.getGenericInterfaces();
    Class<V> resultType = null;
    for(Type t : iface) {
      if(t.toString().contains(ICodexType.class.getSimpleName())) {
        resultType = (Class<V>) ((ParameterizedType)t).getActualTypeArguments()[0];
        break;
      }
    }
    
    return resultType;
  }
  
  
  static CodexFactory inst() {
    return INST;
  }
  private static final CodexFactory INST = new CodexFactory();
  
  private CodexFactory() {
    
  }
  
  public static void main(String[] pArgs) {
    System.out.println(CodexFactory.getTypeOfCodex(ETestData.class));
  }
}
