package com.flybotix.hfr;

import java.util.HashMap;
import java.util.Map;

import com.flybotix.hfr.codex.DefaultCodexReceiver;
import com.flybotix.hfr.codex.encode.AEncoder;
import com.flybotix.hfr.codex.encode.EncoderFactory;
import com.flybotix.hfr.io.Protocols;
import com.flybotix.hfr.io.Protocols.EProtocol;
import com.flybotix.hfr.io.receiver.IMessageParser;
import com.flybotix.hfr.io.receiver.IReceiveProtocol;
import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class TestClientReceiver {
  
  private static ILog LOG = Logger.createLog(TestClientReceiver.class);
  
  public static void main(String[] pArgs) {
    Logger.setLevel(ELevel.INFO);
    AEncoder<Double, ETestData> enc = EncoderFactory.getDoubleEncoder(ETestData.class, true);
    DefaultCodexReceiver<Double, ETestData> codexRecv = new DefaultCodexReceiver<>(enc);
    codexRecv.addListener(codex -> System.out.println(codex));
    
    Map<Integer, IMessageParser<?>> parsers = new HashMap<>();
    parsers.put(ETestData.class.hashCode(), codexRecv);
//    IReceiveProtocol irp = Protocols.createReceiver(EProtocol.UDP, 7777, "", parsers);
    IReceiveProtocol irp = Protocols.createReceiver(EProtocol.TCP, 7777, "", parsers);
  }
}
