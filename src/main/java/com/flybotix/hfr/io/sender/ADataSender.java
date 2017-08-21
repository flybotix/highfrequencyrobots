package com.flybotix.hfr.io.sender;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import com.flybotix.hfr.io.ConnectionStatus;
import com.flybotix.hfr.io.MessageQueue;
import com.flybotix.hfr.io.receiver.ASocketReceiver.ESocketType;
import com.flybotix.hfr.util.lang.Delegator;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public abstract class ADataSender extends Delegator<ConnectionStatus> {
  protected final MessageQueue mMessageQ = new MessageQueue();
  protected int mDestPort;
  protected int mHostPort;
  protected String mDestAddress;
  protected final ConnectionStatus mStatus = new ConnectionStatus(ESocketType.CLIENT);
  private boolean mIsRegisteredWithShutdown = false;

  private ILog mLog = Logger.createLog(ADataSender.class);
  
  public void setHostPort(int pPort) {
    mHostPort = pPort;
    reconnectIfLive();
  }
  
  public void setDestPort(int pPort) {
    mDestPort = pPort;
    reconnectIfLive();
  }
  
  public void setDestAddress(String pAddress) {
    mDestAddress = pAddress;
    reconnectIfLive();
  }
  
  protected abstract void establishConnection(InetAddress addr);

  
  public final void connect() {
    if(!mStatus.isConnected()) {
      try {
        InetAddress addr = InetAddress.getByName(mDestAddress);
        establishConnection(addr);
        if(!mIsRegisteredWithShutdown) {
          Runtime.getRuntime().addShutdownHook(new Thread(() -> disconnect()));
          mIsRegisteredWithShutdown = true;
        }
      } catch (UnknownHostException e) {
        update(mStatus.errorDuringAttempt());
        mLog.error(e.getMessage());
      }
    }
  }
  
  public void disconnect() {
    update(mStatus.expectedDisconnect());
  }

  
  public void sendMessage(int pId, byte[] pMessage) {
    ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + Integer.BYTES + pMessage.length);
    bb.putInt(pId);
    bb.putInt(pMessage.length);
    bb.put(pMessage);
    mLog.debug("Adding message " + pId + " with size " + pMessage.length);
    try {
      mMessageQ.add(bb);
    } catch (InterruptedException e) {
      mLog.error("Dropping message of type " + pId);
      mLog.exception(e);
    }
  }
  
  private void reconnectIfLive() {
    if(mStatus.isConnected() || mStatus.isAttempting()) {
      disconnect();
      connect();
    }
  }
}
