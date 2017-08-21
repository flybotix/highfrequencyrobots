package com.flybotix.hfr.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.flybotix.hfr.io.receiver.AbstractMessageReceiver;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class PassthroughTransport extends AbstractMessageReceiver<Double>{
  public PassthroughTransport() {
    super(Double.class);
  }

  private ILog mLog = Logger.createLog(PassthroughTransport.class);
  private MessageQueue mMessageQ = new MessageQueue();

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

  @Override
  public void disconnect() {
    
  }

  @Override
  public void connect() {
    
  }

  @Override
  public void run() {
    try {
      while (canSend()) {
        List<ByteBuffer> messages = mMessageQ.removeAll();
        for(ByteBuffer bb : messages) {
          mLog.debug("Writing message.");
          byte[] array = bb.array();
          mLog.debug(Arrays.toString(array));
          sendImpl(array);
        }
      }
    } catch (Exception e) {
      mLog.exception(e);
    }
  }
  
  protected boolean canSend() {
    return true;
  }

  protected void sendImpl(byte[] pData) {
    ByteBuffer bb = ByteBuffer.wrap(pData);
    int id = bb.getInt();
    addMessageToQ(id, bb);
  }
}
