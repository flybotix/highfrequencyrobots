package com.flybotix.hfr.io.sender;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UDPSender extends ADataSender{
  
  private final Executor mThreads = Executors.newFixedThreadPool(1);

  @Override
  protected void establishConnection(InetAddress addr) {
    startSendThread(addr);
  }

  private void startSendThread(InetAddress addr) {
    mThreads.execute(() -> {
      while(mStatus.isConnected()) {
        
        try {
          ByteBuffer msg = mMessageQ.removeFirst();
          byte[] arr = msg.array();
          DatagramPacket packet = new DatagramPacket(arr, arr.length, addr, mPort);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
  }
}
