package com.flybotix.hfr;

import com.flybotix.hfr.codex.CodexReceiver;
import com.flybotix.hfr.io.Protocols.EProtocol;
import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class TestClientReceiver {
  
  private static ILog LOG = Logger.createLog(TestClientReceiver.class);
  
  public static void main(String[] pArgs) {
    Logger.setLevel(ELevel.DEBUG);
    
    CodexReceiver<Double, ETestData> codexRecv = new CodexReceiver<>(ETestData.class);
    codexRecv.addListener(codex -> System.out.println(codex));
    codexRecv.startReceiving(EProtocol.UDP, 7777, "");
  }
}
