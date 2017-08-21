package com.flybotix.hfr;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.encode.AEncoder;
import com.flybotix.hfr.codex.encode.EncoderFactory;
import com.flybotix.hfr.io.EConnectionState;
import com.flybotix.hfr.io.sender.TCPSender;
import com.flybotix.hfr.io.sender.UDPSender;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class TestRobotSender {

  private static ILog LOG = Logger.createLog(TestRobotSender.class);

  public static void main(String[] pArgs) throws Exception{
    Codex<ETestData, Double> data = Codex.of.doubles(ETestData.class);
    for(ETestData e : ETestData.values()) {
      data.put(e, e.ordinal() * 10d);
    }
//    testSendViaTCP(data);
    testSendViaUDP(data);
  }

  private static void testSendViaTCP(Codex<ETestData, Double> data) throws InterruptedException {
    TCPSender protocol = new TCPSender();
    protocol.addListener(status -> {
      if(status.getState() == EConnectionState.DISCONNECTED) {
        LOG.debug("Disconnected via: ");
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for(int i =0 ; i < Math.min(stack.length, 8); i++) {
          LOG.debug(stack[i]);
        }
      } else {
        LOG.debug("TCPSender status: " + status.getState());
      }
    });
    protocol.setDestPort(7777);
    protocol.setDestAddress("localhost");
    protocol.connect();
    

    final AEncoder<ETestData, Double> enc = EncoderFactory.getDoubleEncoder(ETestData.class, true);
    System.out.println("Sending " + data);
    protocol.sendMessage(ETestData.class.hashCode(), enc.encode(data));
  }

  private static void testSendViaUDP(Codex<ETestData, Double> data) {
    UDPSender protocol = new UDPSender();
    protocol.setDestAddress("localhost");
    protocol.setHostPort(7778);
    protocol.setDestPort(7777);
    protocol.addListener(update->LOG.debug(update));
    protocol.connect();

    final AEncoder<ETestData, Double> enc = EncoderFactory.getDoubleEncoder(ETestData.class, true);
    System.out.println("Sending " + data);
    protocol.sendMessage(ETestData.class.hashCode(), enc.encode(data));
  }

  private static void testReceipViaNT() {

  }
}
