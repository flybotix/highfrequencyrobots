package com.flybotix.hfr.io.receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class UDPReceiver extends ASocketReceiver {

  private int mMaxBufferSize = 0;
  private DatagramSocket mSocket = null;
  private ILog mLog = Logger.createLog(UDPReceiver.class);

  public UDPReceiver() {
    super(ESocketType.SERVER);
  }

  @Override
  public void connect() {
    update(mStatus.attemptingConnection());
    try {
      mSocket = new DatagramSocket(mPort);
      update(mStatus.connectionEstablished());
    } catch (SocketException e1) {
      mLog.exception(e1);
      update(mStatus.errorDuringAttempt());
    }
  }
  
  @Override
  public void run() {

    // buffer to receive incoming data
    byte[] buffer = new byte[mMaxBufferSize];
    DatagramPacket incoming = new DatagramPacket(buffer, mMaxBufferSize);

    while (mStatus.isConnected() && mSocket != null) {
      try {
        mSocket.receive(incoming);
        update(mStatus.periodicUpdate(true));
        byte[] receivedData = new byte[incoming.getLength()];
        System.arraycopy(incoming.getData(), 0, receivedData, 0, receivedData.length);
        incoming.setLength(mMaxBufferSize);
        
        // At this point we have a length, but not a type.  Presume the first integer is the type.
        int msgSize = receivedData.length - Integer.BYTES;
        
        // Wrap the integer region in its own buffer and decode it.
        int msgId = ByteBuffer.wrap(receivedData, 0, Integer.BYTES).getInt();
        
        // Then wrap the remaining region in a separate buffer as the message.
        ByteBuffer msg = ByteBuffer.wrap(receivedData, Integer.BYTES, msgSize);
        addMessageToQ(msgId, msg);
      } catch (IOException e) {
        mLog.exception(e);
        update(mStatus.unexpectedDisconnect());
      } finally {
        if(mSocket != null) {
          mSocket.close();
        }
      }
    }
  }

  @Override
  public void addParserForMessageType(Integer pType, IMessageParser<?> pParser) {
    mMaxBufferSize = Math.max(mMaxBufferSize, pParser.getBufferSize() + Integer.BYTES);
    mMessageParsers.put(pType, pParser);
  }

}
