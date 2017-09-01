package com.flybotix.hfr;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;
import com.flybotix.hfr.codex.CodexReceiver;
import com.flybotix.hfr.codex.CodexSender;
import com.flybotix.hfr.io.Protocols.EProtocol;

public class DemoCode {
  public enum RobotData implements CodexOf<Double>{
    pdb0,
    pdb1,
    pdb2,
    pdb3,
    vrm0,
    totalcurrent
  }
  
  public void createReceiver () {

    CodexReceiver<Double, ETestData> receiver = new CodexReceiver<>(ETestData.class);
    receiver.startReceiving(EProtocol.UDP, 7777, "");
    receiver.addListener(codex -> System.out.println(codex));
  }
  
  public void createSender () {

    CodexSender<Double, RobotData> sender = new CodexSender<>(RobotData.class, true);
    sender.initConnection(EProtocol.UDP, 7778, 7777, "localhost");
    
    
    Codex<Double, RobotData> data = Codex.of.thisEnum(RobotData.class);
    for(RobotData e : RobotData.values()) {
      data.set(e, e.ordinal() * 10d * Math.PI);
    }
    System.out.println("Sending " + data);
    
    sender.send(data);
    
    data.reset();
    data.set(RobotData.pdb2, -23.3d);
    sender.send(data);
    
  
  }
}
