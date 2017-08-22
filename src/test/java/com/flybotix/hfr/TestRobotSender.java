package com.flybotix.hfr;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexSender;
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
    Logger.setLevel(ELevel.DEBUG);
    
    Codex<Double, ETestData> data = Codex.of.thisEnum(ETestData.class);
    for(ETestData e : ETestData.values()) {
      data.put(e, e.ordinal() * 10d * Math.PI);
    }
    System.out.println("Sending " + data);
    
    CodexSender<Double, ETestData> sender = new CodexSender<>(ETestData.class, true);
    sender.initConnection(EProtocol.UDP, 7778, 7777, "localhost");
    sender.send(data);
    
    data.reset();
    data.put(ETestData.pdb2, -23.3d);
    sender.send(data);
  }
}
