package com.flybotix.hfr;

import java.util.ArrayList;
import java.util.List;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.DefaultCodexReceiver;
import com.flybotix.hfr.io.encode.AbstractEncoder;
import com.flybotix.hfr.io.encode.EncoderFactory;
import com.flybotix.hfr.io.receiver.AbstractSocketReceiver.ESocketType;
import com.flybotix.hfr.io.receiver.TCPReceiver;

public class TestClientReceiver {
  
  private static List<Codex<ETestData, Double>> data = new ArrayList<>();
  private static Codex<ETestData, Double> latest = null; 
  
  public static void main(String[] pArgs) {
    AbstractEncoder<ETestData, Double> enc = EncoderFactory.getDoubleEncoder(ETestData.class, true);
    DefaultCodexReceiver<ETestData, Double> codexRecv = new DefaultCodexReceiver<>(enc);
    codexRecv.addListener(codex -> System.out.println(codex));
    testReceiptViaTCP(codexRecv);
  }

  private static void testReceiptViaTCP(DefaultCodexReceiver<ETestData, Double> recv) {
    TCPReceiver protocol = new TCPReceiver(ESocketType.SERVER);
    protocol.setPort(7777);
    protocol.setIpAddress("localhost");
    protocol.addListener(status -> System.out.println("TCPReceiver status: " + status.getState()));
    protocol.addParserForMessageType(ETestData.class, recv);
    protocol.connect();
    new Thread(protocol).start();
  }
  
  private static void testReceiptViaUDP() {
    
  }
  
  private static void testReceipViaNT() {
    
  }
}
