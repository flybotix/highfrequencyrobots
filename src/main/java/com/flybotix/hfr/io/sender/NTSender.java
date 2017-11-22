package com.flybotix.hfr.io.sender;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.flybotix.hfr.io.MessageProtocols;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class NTSender extends ADataSender {
  private final Executor mThreads = Executors.newFixedThreadPool(1);
  
  private NetworkTable mTable = null;

  @Override
  protected boolean usesNetAddress() {
    return false;
  }
  
  @Override
  protected void establishConnection(String addr) {
    mTable = NetworkTable.getTable(addr);
    startSendThread();
  }

  @Override
  protected void establishConnection(InetAddress addr) {
    // Not used
  }

  private void startSendThread() {
    mThreads.execute(() -> {
      while(mStatus.isConnected()) {
        ByteBuffer msg = null;
        try {
          Thread.sleep((long)(1000d/MessageProtocols.MAX_PACKET_RATE_HZ));
          msg = mMessageQ.removeFirst();
          if(msg != null) {
            byte[] arr = msg.array();
            mTable.putRaw(MessageProtocols.NT_ELEMENT_NAME, arr);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    });
  }
}
