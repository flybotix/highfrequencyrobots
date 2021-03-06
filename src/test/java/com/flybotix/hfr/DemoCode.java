package com.flybotix.hfr;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexOf;
import com.flybotix.hfr.codex.CodexReceiver;
import com.flybotix.hfr.codex.CodexSender;
import com.flybotix.hfr.io.MessageProtocols;
import com.flybotix.hfr.io.MessageProtocols.EProtocol;
import com.flybotix.hfr.io.receiver.IReceiveProtocol;
import com.flybotix.hfr.io.sender.ISendProtocol;

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
    IReceiveProtocol protocol = MessageProtocols.createReceiver(EProtocol.UDP, 7778, "localhost");
    CodexReceiver<Double, ETestData> receiver = new CodexReceiver<>(ETestData.class, protocol);
    receiver.addListener(codex -> System.out.println(codex));
  }
  
  public void createSender () {
    
    ISendProtocol protocol = MessageProtocols.createSender(EProtocol.UDP, 7778, 7777, "localhost");
    CodexSender sender = new CodexSender(protocol);
    
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
