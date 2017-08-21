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
      mSocket = new DatagramSocket(mHostPort);
      mLog.debug("Local port: ",mHostPort);
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
        byte[] msgIdBuffer = new byte[Integer.BYTES];
        System.arraycopy(incoming.getData(), 0, msgIdBuffer, 0, Integer.BYTES);
        int msgId = ByteBuffer.wrap(msgIdBuffer).getInt();
        
        byte[] msgSizeBuffer = new byte[Integer.BYTES];
        System.arraycopy(incoming.getData(), Integer.BYTES, msgSizeBuffer, 0, Integer.BYTES);
        int msgSize = ByteBuffer.wrap(msgSizeBuffer).getInt();

        byte[] receivedData = new byte[msgSize];
        System.arraycopy(incoming.getData(), 2*Integer.BYTES, receivedData, 0, msgSize);
        
        // remember to reset the length of the packet to its maximum size every time
        incoming.setLength(mMaxBufferSize);

        ByteBuffer msg = ByteBuffer.wrap(receivedData);
        // Then wrap the remaining region in a separate buffer as the message.
        mLog.debug("Received message ", msgId, " with size ", msgSize, ": ", Arrays.toString(receivedData));
        addMessageToQ(msgId, msg);
      } catch (IOException e) {
        mLog.exception(e);
        update(mStatus.unexpectedDisconnect());
      }
    }
    if(mSocket != null) {
      mSocket.close();
    }
  }

  @Override
  public void addParserForMessageType(Integer pType, IMessageParser<?> pParser) {
    mMaxBufferSize = Math.max(mMaxBufferSize, pParser.getBufferSize() + 2*Integer.BYTES);
    mLog.info("Registering msg " + pType + " with buffer size " + pParser.getBufferSize());
    mMessageParsers.put(pType, pParser);
  }

}
