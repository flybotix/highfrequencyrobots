package com.flybotix.hfr.io.sender;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.flybotix.hfr.io.ConnectionStatus;
import com.flybotix.hfr.io.EStaticMessageIds;
import com.flybotix.hfr.io.MessageQueue;
import com.flybotix.hfr.io.MessageProtocols;
import com.flybotix.hfr.util.lang.Delegator;
import com.flybotix.hfr.util.log.ELevel;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public abstract class ADataSender extends Delegator<ConnectionStatus> implements ISendProtocol{
  protected final MessageQueue mMessageQ = new MessageQueue();
  private final MessageQueue mBatchQ = new MessageQueue();
  protected int mDestPort;
  protected int mHostPort;
  protected String mDestAddress;
  protected final ConnectionStatus mStatus = new ConnectionStatus();
  protected boolean mIsBatching = false;
  private boolean mIsRegisteredWithShutdown = false;
  
  private final Timer mBatchTimer = new Timer("Batch Writer");
  private TimerTask mBatchTask = null;

  private ILog mLog = Logger.createLog(ADataSender.class);

  @Override
  public void setHostPort(int pPort) {
    mHostPort = pPort;
    reconnectIfLive();
  }

  @Override
  public void setDestPort(int pPort) {
    mDestPort = pPort;
    reconnectIfLive();
  }
  
  @Override
  public void setDestAddress(String pAddress) {
    mDestAddress = pAddress;
    reconnectIfLive();
  }
  
  @Override
  public void setBatching(boolean pUseBatching) {
    mIsBatching = pUseBatching;
    if(mIsBatching && mBatchTask == null) {
      mBatchTask = new BatchTask(mMessageQ, mBatchQ);
      long period = (long)Math.ceil(1000d/MessageProtocols.MAX_PACKET_RATE_HZ);
      mBatchTimer.scheduleAtFixedRate(mBatchTask, period, period);
    } else if(!mIsBatching){
      if(mBatchTask != null) {
        mBatchTask.cancel();
        mBatchTask = null;
      }
    }
  }
  
  protected abstract void establishConnection(InetAddress addr);
  protected abstract void establishConnection(String addr);
  protected abstract boolean usesNetAddress();


  @Override
  public final void connect() {
    if(!mStatus.isConnected()) {
      if(usesNetAddress()) {
        establishConnection(mDestAddress);
      } else {
        try {
          InetAddress addr = InetAddress.getByName(mDestAddress);
          establishConnection(addr);
        } catch (UnknownHostException e) {
          update(mStatus.errorDuringAttempt());
          mLog.error(e.getMessage());
        }
      }
      if(!mIsRegisteredWithShutdown) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> disconnect()));
        mIsRegisteredWithShutdown = true;
      }
    }
  }

  @Override
  public void disconnect() {
    update(mStatus.expectedDisconnect());
  }

  @Override
  public void sendMessage(int pId, byte[] pMessage) {
    ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + Integer.BYTES + pMessage.length);
    bb.putInt(pId);
    bb.putInt(pMessage.length);
    bb.put(pMessage);
    if(Logger.isEnabled(ELevel.DEBUG)) {
      mLog.debug("Adding message " + pId + " with body size " + pMessage.length + " (raw size = " + bb.limit() + ")");
    }
    try {
      if(mIsBatching) {
        mBatchQ.add(bb);
      } else {
        mMessageQ.add(bb);
      }
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
  
  private final class BatchTask extends TimerTask {
    private final MessageQueue batch;
    private final MessageQueue send;
    public BatchTask(MessageQueue pSendQueue, MessageQueue pBatchQueue) {
      batch = pBatchQueue;
      send = pSendQueue;
    }
    
    @Override
    public boolean cancel() {
      try {
        if(!batch.isEmpty()) {
          // wait 2 cycles for the batch to clear, then afterwards just clear it
          Thread.sleep((long)Math.ceil(2000d/MessageProtocols.MAX_PACKET_RATE_HZ));
          batch.removeAll();
        }
      } catch (InterruptedException e) {
        // so there's an error, but tbh it's a sticky mess if the cancel thread gets interrupted 
      }
      return super.cancel();
    }
    
    @Override
    public void run() {
      try {
        // Format: 
        // 1- int = EStaticMessageId.BATCHED_MESSAGE.ordinal()
        // 1- int = Calculated message size
        // 1- int = Number of batched messages
        // remaining = batched messages
        List<ByteBuffer> buffersToSend = batch.removeAllMessageUpToSize(MessageProtocols.MAX_PACKET_SIZE_BYTES - (3 * Integer.BYTES));
        if(MessageQueue.getTotalMessageSize(buffersToSend) >= MessageProtocols.MAX_PACKET_SIZE_BYTES * 0.95) {
          mLog.warn("Batch packet buffer is close to full.  If this is common consider adjusting Protocols.MAX_PACKET_RATE_HZ.");
        }
        if(!buffersToSend.isEmpty()) {
          int msgSize = MessageQueue.getTotalMessageSize(buffersToSend);
          ByteBuffer msg = ByteBuffer.allocate(msgSize + 3 * Integer.BYTES);
          msg.putInt(EStaticMessageIds.BATCHED_MESSAGE.ordinal());
          msg.putInt(msgSize + Integer.BYTES); // Include the # of embedded messages in this size
          msg.putInt(buffersToSend.size());
          for(ByteBuffer bb : buffersToSend) {
            msg.put(bb.array());
          }
          if(Logger.isEnabled(ELevel.INFO)) {
            mLog.info("Sending batched message of size " + msgSize + " with batch header size of " + 3*Integer.BYTES);
          }
          send.add(msg);
        }
      } catch (InterruptedException e) {
        // Not a big deal - this task will re-run at a fixed rate
      }
    }
    
  }
}
