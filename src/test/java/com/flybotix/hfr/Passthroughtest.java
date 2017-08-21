package com.flybotix.hfr;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.DefaultCodexReceiver;
import com.flybotix.hfr.codex.encode.AEncoder;
import com.flybotix.hfr.codex.encode.EncoderFactory;
import com.flybotix.hfr.io.PassthroughTransport;

public class Passthroughtest {

  public static void main(String[] pArgs) throws Exception{
    Codex<ETestData, Double> data = Codex.of.doubles(ETestData.class);
    for(ETestData e : ETestData.values()) {
      data.put(e, e.ordinal() * 10d);
    }
    
    AEncoder<ETestData, Double> enc = EncoderFactory.getDoubleEncoder(ETestData.class, true);
    DefaultCodexReceiver<ETestData, Double> dcr = new DefaultCodexReceiver<>(enc);
    PassthroughTransport pt = new PassthroughTransport();
    pt.addParserForMessageType(ETestData.class, dcr);
    dcr.addListener(codex -> System.out.println(codex));
    new Thread(pt).run();
    
    pt.sendMessage(ETestData.class.hashCode(), enc.encode(data));
  }
}
