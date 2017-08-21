package com.flybotix.hfr;

import java.util.ArrayList;
import java.util.List;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.io.encode.AbstractEncoder;
import com.flybotix.hfr.io.encode.EncoderFactory;
import com.flybotix.hfr.io.receiver.EConnectionState;
import com.flybotix.hfr.io.sender.TCPSender;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class TestRobotSender {
  
  private static List<Codex<ETestData, Double>> data = new ArrayList<>();
  private static Codex<ETestData, Double> latest = null; 
  private static ILog LOG = Logger.createLog(TestRobotSender.class);
  
  public static void main(String[] pArgs) throws Exception{
    Codex<ETestData, Double> data = Codex.of.doubles(ETestData.class);
    for(ETestData e : ETestData.values()) {
      data.put(e, e.ordinal() * 10d);
    }
    testSendViaTCP(data);
  }

  private static void testSendViaTCP(Codex<ETestData, Double> data) throws InterruptedException {
    TCPSender protocol = new TCPSender();
    protocol.setPort(7777);
    protocol.setIpAddress("localhost");
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
    protocol.connect();
    new Thread(protocol).start();
    Thread.sleep(250);

    AbstractEncoder<ETestData, Double> enc = EncoderFactory.getDoubleEncoder(ETestData.class, true);

    System.out.println("Sending " + data);
    protocol.sendMessage(ETestData.class.hashCode(), enc.encode(data));
    
    Thread.sleep(10000);
  }
  
  private static void testReceiptViaUDP() {
    
  }
  
  private static void testReceipViaNT() {
    
  }
}
