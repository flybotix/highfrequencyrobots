package com.flybotix.hfr;

import java.util.ArrayList;
import java.util.List;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.DefaultCodexReceiver;
import com.flybotix.hfr.codex.encode.AEncoder;
import com.flybotix.hfr.codex.encode.EncoderFactory;
import com.flybotix.hfr.io.receiver.ASocketReceiver.ESocketType;
import com.flybotix.hfr.io.receiver.TCPReceiver;
import com.flybotix.hfr.io.receiver.UDPReceiver;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class TestClientReceiver {
  
  private static List<Codex<ETestData, Double>> data = new ArrayList<>();
  private static Codex<ETestData, Double> latest = null; 
  private static ILog LOG = Logger.createLog(TestClientReceiver.class);
  
  public static void main(String[] pArgs) {
    AEncoder<ETestData, Double> enc = EncoderFactory.getDoubleEncoder(ETestData.class, true);
    DefaultCodexReceiver<ETestData, Double> codexRecv = new DefaultCodexReceiver<>(enc);
    codexRecv.addListener(codex -> System.out.println(codex));
    testReceiptViaUDP(codexRecv);
  }

  private static void testReceiptViaTCP(DefaultCodexReceiver<ETestData, Double> recv) {
    TCPReceiver protocol = new TCPReceiver(ESocketType.SERVER);
    protocol.setPort(7777);
    protocol.setIpAddress("localhost");
    protocol.addListener(status -> LOG.debug("TCPReceiver status: " + status.getState()));
    protocol.addParserForMessageType(ETestData.class, recv);
    protocol.connect();
    new Thread(protocol).start();
  }
  
  private static void testReceiptViaUDP(DefaultCodexReceiver<ETestData, Double> recv) {
    UDPReceiver protocol = new UDPReceiver();
    protocol.setIpAddress("localhost");
    protocol.setPort(7777);
    protocol.addListener(status -> LOG.debug("TCPReceiver status: " + status.getState()));
    protocol.addParserForMessageType(ETestData.class, recv);
    protocol.connect();
    new Thread(protocol).start();
  }
  
  private static void testReceipViaNT() {
    
  }
}
