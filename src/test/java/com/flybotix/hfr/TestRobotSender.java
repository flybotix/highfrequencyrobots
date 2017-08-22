package com.flybotix.hfr;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexFactory;
import com.flybotix.hfr.codex.encode.AEncoder;
import com.flybotix.hfr.io.Protocols;
import com.flybotix.hfr.io.Protocols.EProtocol;
import com.flybotix.hfr.io.sender.ISendProtocol;
import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class TestRobotSender {

  private static ILog LOG = Logger.createLog(TestRobotSender.class);

  public static void main(String[] pArgs) throws Exception{
    Logger.setLevel(ELevel.INFO);
    Codex<Double, ETestData> data = Codex.of.doubles(ETestData.class);
    for(ETestData e : ETestData.values()) {
      data.put(e, e.ordinal() * 10d * Math.PI);
    }
    final AEncoder<Double, ETestData> enc = CodexFactory.getDoubleEncoder(ETestData.class, true);
    System.out.println("Sending " + data);
    
    
//    ISendProtocol isp = Protocols.createSender(EProtocol.UDP, 7778, 7777, "localhost");
    ISendProtocol isp = Protocols.createSender(EProtocol.TCP, 7778, 7777, "localhost");
    isp.addListener(update->LOG.debug(update));
    isp.sendMessage(ETestData.class.hashCode(), enc.encode(data));
  }
}
