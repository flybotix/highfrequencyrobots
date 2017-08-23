package com.flybotix.hfr;

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
    Protocols.MAX_PACKET_RATE_HZ = MAX_PACKET_RATE_HZ;
    CodexReceiver<Double, ETestData> codexRecv = new CodexReceiver<>(ETestData.class);
    if(TEST_HIGH_FREQUENCY_DATA_OVER_SOCKET) {
      codexRecv.addListener(new CodexListener());
    } else {
      Logger.setLevel(ELevel.DEBUG);
      codexRecv.addListener(codex -> LOG.warn(codex));
    }
    codexRecv.startReceiving(TEST_SOCKET_PROTOCOL, TEST_RECEIVER_PORT, "");
  }
  
  private static class CodexListener implements IUpdate<Codex<Double, ETestData>> {
    private static final double pollingPeriodSecs = 2d;
    private double mCount = 0;
    private long mLast = 0;
    private long mPeriod = 0;
    public void update(Codex<Double, ETestData> codex) {
      mCount++;
      long now = System.currentTimeMillis();
      if(now - mLast >= 1000 * pollingPeriodSecs) {
        mPeriod = (now - mLast)/1000;
        report();
        mCount = 0;
        mLast = now;
      }
    }
    
    private void report() {
      LOG.warn("Rate (msgs/sec): " + (mCount / mPeriod));
    }
  }
}
