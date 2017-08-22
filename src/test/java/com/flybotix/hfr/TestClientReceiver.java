package com.flybotix.hfr;

import com.flybotix.hfr.codex.DefaultCodexReceiver;
import com.flybotix.hfr.io.Protocols.EProtocol;
import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class TestClientReceiver {
  
  private static ILog LOG = Logger.createLog(TestClientReceiver.class);
  
  public static void main(String[] pArgs) {
    Logger.setLevel(ELevel.DEBUG);
    
    DefaultCodexReceiver<Double, ETestData> codexRecv = new DefaultCodexReceiver<>(ETestData.class);
    codexRecv.addListener(codex -> System.out.println(codex));
    codexRecv.startReceiving(EProtocol.TCP, 7777, "");
    
  }
}
