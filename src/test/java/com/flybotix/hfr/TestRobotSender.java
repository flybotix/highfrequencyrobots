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
    Logger.setLevel(ELevel.DEBUG);
    Protocols.MAX_PACKET_RATE_HZ = MAX_PACKET_RATE_HZ;
    
    CodexSender sender = new CodexSender();
    sender.initConnection(TEST_SOCKET_PROTOCOL, TEST_SENDER_PORT, TEST_RECEIVER_PORT, TEST_RECEIVER_HOST_NAME);
    LOG.info("Sending data to " + TEST_RECEIVER_HOST_NAME + ":" + TEST_RECEIVER_PORT);
    if(TEST_HIGH_FREQUENCY_DATA_OVER_SOCKET) {
      Logger.setLevel(ELevel.WARN);
      testHighFrequency(sender, TEST_HIGH_FREQUENCY_DATA_RATE_HZ);
    } else {
      Logger.setLevel(ELevel.DEBUG);
      testSingle(sender);
    }
  }
  
  private static void testHighFrequency(CodexSender sender, double pRateHz) {
    double totalRate = 0d;
    for(int i = 0; i < TEST_HIGH_FREQUENCY_DATA_NUM_SEND_THREADS; i++) {
      final Codex<Double, ETestData> data = Codex.of.thisEnum(ETestData.class);
      double rand = Math.ceil((Math.random()-0.5d) * TEST_HIGH_FREQUENCY_DATA_RATE_RAND * pRateHz) + pRateHz;
      totalRate += rand;
      final long periodMs = (long)Math.ceil(1000d/rand);
      LOG.warn("Thread " + i + " sending data at " + rand + "hz");
      Timer t = new Timer("SEND THREAD " + i);
      TimerTask tt = new TimerTask() {
        private double mCount = 0;
        public void run() {
          data.reset();
          mCount++;
          for(ETestData e : ETestData.values()) {
            data.set(e, e.ordinal() * mCount * Math.PI);
          }
          try {
            sender.send(data);
          } catch (Throwable t) {
            t.printStackTrace();
            try {
              Thread.sleep(50);
            } catch (InterruptedException e1) {
              // TODO Auto-generated catch block
              e1.printStackTrace();
            }
            System.exit(-1);
          }
        }
      };
      t.scheduleAtFixedRate(tt, 0, periodMs);
    }
    LOG.warn("Total (theoretical) message throughput (msgs/sec): " + totalRate);
  }
  
  private static void testSingle(CodexSender sender) {
    
    Codex<Double, ETestData> data = Codex.of.thisEnum(ETestData.class);
    for(ETestData e : ETestData.values()) {
      data.set(e, e.ordinal() * Math.PI);
    }
    LOG.debug("Sending " + data);
    
    sender.send(data);
    
//    data.reset();
//    data.set(ETestData.pdb2, -23.3d);
//    System.out.println("Sending " + data);
//    sender.send(data);
    
  }
}
