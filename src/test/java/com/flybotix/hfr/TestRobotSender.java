package com.flybotix.hfr;

import java.util.Timer;
import java.util.TimerTask;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexSender;
import com.flybotix.hfr.io.Protocols;
import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class TestRobotSender implements TestConfig{

  private static ILog LOG = Logger.createLog(TestRobotSender.class);

  public static void main(String[] pArgs) throws Exception{
    Protocols.MAX_PACKET_RATE_HZ = MAX_PACKET_RATE_HZ;
    
    CodexSender<Double, ETestData> sender = new CodexSender<>(ETestData.class, true);
    sender.initConnection(TEST_SOCKET_PROTOCOL, TEST_SENDER_PORT, TEST_RECEIVER_PORT, TEST_RECEIVER_HOST_NAME);
    
    if(TEST_HIGH_FREQUENCY_DATA_OVER_SOCKET) {
      Logger.setLevel(ELevel.WARN);
      testHighFrequency(sender, TEST_HIGH_FREQUENCY_DATA_RATE_HZ);
    } else {
      Logger.setLevel(ELevel.DEBUG);
      testSingle(sender);
    }
  }
  
  private static void testHighFrequency(CodexSender<Double, ETestData> sender, double pRateHz) {
    LOG.warn("Sending data at " + pRateHz + "hz");
    final Codex<Double, ETestData> data = Codex.of.thisEnum(ETestData.class);
    final long periodMs = (long)(1000d/pRateHz);
    Timer t = new Timer();
    TimerTask tt = new TimerTask() {
      private double mCount = 0;
      public void run() {
        data.reset();
        mCount++;
        for(ETestData e : ETestData.values()) {
          data.put(e, e.ordinal() * mCount * Math.PI);
        }
        sender.send(data);
      }
    };
    t.scheduleAtFixedRate(tt, 0, periodMs);;
  }
  
  private static void testSingle(CodexSender<Double, ETestData> sender) {
    
    Codex<Double, ETestData> data = Codex.of.thisEnum(ETestData.class);
    for(ETestData e : ETestData.values()) {
      data.put(e, e.ordinal() * 10d * Math.PI);
    }
    System.out.println("Sending " + data);
    
    sender.send(data);
    
    data.reset();
    data.put(ETestData.pdb2, -23.3d);
    sender.send(data);
    
  }
}
