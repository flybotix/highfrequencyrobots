package com.flybotix.hfr.io.receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class UDPReceiver extends ASocketReceiver {

  private DatagramSocket mSocket = null;
  private static final ILog mLog = Logger.createLog(UDPReceiver.class);

  @Override
  public void establishConnection() {
    update(mStatus.attemptingConnection());
    try {
      mSocket = new DatagramSocket(mHostPort);
      mLog.debug("Local port: ",mHostPort);
      update(mStatus.connectionEstablished());
      startReadThread();
    } catch (SocketException e1) {
      mLog.exception(e1);
      update(mStatus.errorDuringAttempt());
    }
  }
  
  private void startReadThread() {
    mConnectionThreads.execute(() -> {
      // buffer to receive incoming data
      byte[] buffer = new byte[mMaxBufferSize];
      DatagramPacket incoming = new DatagramPacket(buffer, mMaxBufferSize);

      while (mStatus.isConnected() && mSocket != null) {
        try {
          // remember to reset the length of the packet to its maximum size every time
          incoming.setLength(mMaxBufferSize);
          mSocket.receive(incoming);
          byte[] msgIdBuffer = new byte[Integer.BYTES];
          System.arraycopy(incoming.getData(), 0, msgIdBuffer, 0, Integer.BYTES);
          int msgId = ByteBuffer.wrap(msgIdBuffer).getInt();

          byte[] msgSizeBuffer = new byte[Integer.BYTES];
          System.arraycopy(incoming.getData(), Integer.BYTES, msgSizeBuffer, 0, Integer.BYTES);
          int msgSize = ByteBuffer.wrap(msgSizeBuffer).getInt();
          mLog.debug("Received msg id " + msgId + " with msg size " + msgSize + " (total packet size: " + incoming.getData().length + ")");

          byte[] receivedData = new byte[msgSize];
          System.arraycopy(incoming.getData(), 2*Integer.BYTES, receivedData, 0, msgSize);


          ByteBuffer msg = ByteBuffer.wrap(receivedData);
          // Then wrap the remaining region in a separate buffer as the message.
          mLog.debug("Received message ", msgId, " with  size ", msgSize, ": ", Arrays.toString(receivedData));
          addMessageToQ(msgId, msg);
        } catch (IOException e) {
          mLog.exception(e);
          update(mStatus.unexpectedDisconnect());
        }
      }
      if(mSocket != null) {
        mSocket.close();
      }
    });
  }
}
