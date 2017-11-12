package com.flybotix.hfr;

import java.text.DecimalFormat;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexReceiver;
import com.flybotix.hfr.io.Protocols;
import com.flybotix.hfr.util.lang.IUpdate;
import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class TestClientReceiver implements TestConfig{
  
  private static ILog LOG = Logger.createLog(TestClientReceiver.class);
  
  public static void main(String[] pArgs) {
    Logger.setLevel(ELevel.DEBUG);
    Protocols.MAX_PACKET_RATE_HZ = MAX_PACKET_RATE_HZ;
    CodexReceiver<Double, ETestData> codexRecv = new CodexReceiver<>(ETestData.class);
    if(TEST_HIGH_FREQUENCY_DATA_OVER_SOCKET) {
      Logger.setLevel(ELevel.INFO);
      codexRecv.addListener(new CodexListener());
    } else {
      Logger.setLevel(ELevel.DEBUG);
      codexRecv.addListener(codex -> LOG.warn(codex));
    }
    codexRecv.startReceiving(TEST_SOCKET_PROTOCOL, TEST_RECEIVER_PORT, "");
  }
  
  private static final DecimalFormat df = new DecimalFormat("0.00");
  
  private static class CodexListener implements IUpdate<Codex<Double, ETestData>> {
    private static final double pollingPeriodSecs = 2d;
    private double mCount = 0;
    private double mSize = 0;
    private long mLast = 0;
    private long mPeriod = 0;
    private int mLength = 0;
    public void update(Codex<Double, ETestData> codex) {
      mCount++;
      mLength = codex.length();
      long now = System.currentTimeMillis();
      mSize += codex.encode().length;
      if(now - mLast >= 1000 * pollingPeriodSecs) {
        mPeriod = (now - mLast)/1000;
        report();
        mCount = 0;
        mSize = 0;
        mLast = now;
      }
    }
    
    private void report() {
      System.out.println("Rate (msgs/sec): " + (mCount / mPeriod) 
        + "\t# of Fields: " + mLength 
        + "\tBandwidth (Megabits/sec): " + df.format(mSize/mPeriod/1024d/1024d*8));
    }
  }
}
