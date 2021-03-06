package com.flybotix.hfr.io.sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.flybotix.hfr.io.MessageProtocols;
import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class UDPSender extends ADataSender{

  private final Executor mThreads = Executors.newFixedThreadPool(1);
  private DatagramSocket socket = null;

  private ILog mLog = Logger.createLog(UDPSender.class);

  @Override
  protected boolean usesNetAddress() {
    return true;
  }

  @Override
  protected void establishConnection(String addr) {
    // Not used
  }

  @Override
  protected void establishConnection(InetAddress addr) {
    try {
      socket = new DatagramSocket(mHostPort);
      mLog.debug("Established local socket at ", mHostPort, " with a destination of ", addr, ":", mDestPort);
      update(mStatus.connectionEstablished());
    } catch (SocketException e) {
      e.printStackTrace();
    }
    startSendThread(addr);
  }

  private void startSendThread(final InetAddress addr) {
    mThreads.execute(() -> {
      while(mStatus.isConnected()) {
        ByteBuffer msg = null;
        try {
          Thread.sleep((long)(1000d/MessageProtocols.MAX_PACKET_RATE_HZ));
          msg = mMessageQ.removeFirst();
          if(msg != null) {
            byte[] arr = msg.array();
            if(Logger.isEnabled(ELevel.DEBUG)) {
              mLog.debug("Sending  to " + addr + ":" + mDestPort + "\t" + arr.length + " raw bytes ", Arrays.toString(arr));
            }
            DatagramPacket packet = new DatagramPacket(arr, arr.length, addr, mDestPort);
            socket.send(packet);
          }
        } catch (InterruptedException e) {
          mLog.warn("Interrupted while pulling data off the message queue");
          Thread.currentThread().interrupt();
        } catch (IOException e) {
          if(msg != null) {
            try {
              mMessageQ.addFirst(msg);
            } catch (InterruptedException e1) {
              mLog.error("Interrupted while restoring a message to the queue after connection error.  Message lost.");
            }
          }
          update(mStatus.unexpectedDisconnect());
          mLog.exception(e);
        }
      }
    });
  }
}
