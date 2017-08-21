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
  protected int mPort;
  protected String mIpAddress;
  protected final ConnectionStatus mStatus = new ConnectionStatus(ESocketType.CLIENT);

  private ILog mLog = Logger.createLog(ADataSender.class);
  
  public void setPort(int pPort) {
    mPort = pPort;
    reconnectIfLive();
  }
  
  public void setIpAddress(String pAddress) {
    mIpAddress = pAddress;
    reconnectIfLive();
  }
  
  protected abstract void establishConnection(InetAddress addr);

  
  public final void connect() {
    if(!mStatus.isConnected()) {
      try {
        InetAddress addr = InetAddress.getByName(mIpAddress);
        establishConnection(addr);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> disconnect()));
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
