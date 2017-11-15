package com.flybotix.hfr.io.receiver;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.flybotix.hfr.io.EStaticMessageIds;
import com.flybotix.hfr.io.MessageQueue;
import com.flybotix.hfr.io.Protocols;
import com.flybotix.hfr.util.lang.Delegator;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

/**
 * This class extends a delegator for type T, whatever an implementing class wants the status
 * to be.  The delegator class is a simple listener/update class.
 * 
 * This class implements IDataReceiver, which extends Runnable.  IDataReceiver promises a client methods for
 * connect, disconnect, and adding parsers for messages (the parsers double as listeners).  
 * It also allows a client to put the receivers into their own
 * separate threads.  AbstractMessageReceiver will deal with pulling messages off of the socket thread(s) and
 * decoding them on its own thread(s) - i.e. a client (a.k.a. IMessgeParser) doesn't need to worry about the
 * networking threads.  In addition, when a client/IMessageParser has a read() triggered, the client can
 * store the resulting data structure locally (in addition to updating displays on the display threads).
 * 
 * The classes which extend AbstractMessageReceiver should do the primary socket/passthrough reads in the
 * run() method (from Runnable), and then call addMessageToQ() once for each message.  This allows an implementing
 * class to deal with batching or not deal with batching on its own.
 * 
 * @param <T> - For the default TCP and UDP sockets, T is type ConnectionStatus. Any client which cares about 
 * connection status can add itself as a listener.  
 * 
 * However,
 * in cases where 'connection status' doesn't matter for an individual receiver (such as NetworkTables, 
 * or some UDP setups) then T can turn into the actual message received. The implementing class decides.
 * If Class T equals the decoded object's class, then any listener who was added to this class will
 * be notified of the incoming message (which is of type T).
 */
public abstract class ADataReceiver <T> extends Delegator<T> implements IReceiveProtocol {
  private static final ILog mLog = Logger.createLog(ADataReceiver.class);
  protected final Map<Integer, MessageQueue> mMessageQ = new HashMap<>();
  protected final Map<Integer, IMessageParser<?>> mMessageParsers = new HashMap<>();
  protected long mDecodeRateMs = 1;
  private boolean mIsRegisteredWithShutdown = false;
  protected int mHostPort;
  protected String mHostInfo;
  
  // These are implementation-specific variables, but we need to house them here to support
  // automatic batching
  protected int mMaxBufferSize = 0;
  protected Map<Integer, byte[]> mMessageBuffers = new HashMap<>();
  
  private static final Executor sRECEIVER_THREADS = 
    Executors.newCachedThreadPool(r -> new Thread(r, "Message Queue Decoding Thread"));
  
  
  public ADataReceiver(final Class<T> pType) {
    
    // Start the polling thread which pulls messages off the queue and pushes them to the
    // parser/listeners
    sRECEIVER_THREADS.execute(()->{
      Set<Integer> keys = new HashSet<>();
      while(true) {
        keys.clear();
        synchronized(mMessageQ) {
          // Concurrency trick - if a new MessageQ (i.e. msgid) is added as we iterate,
          // then we'll pick it up on the next pass.  That way we keep the synchronzied
          // block length to a minimum.
          keys.addAll(mMessageQ.keySet());
        }
        for(Integer msgId : keys) {
          MessageQueue q = mMessageQ.get(msgId);
          if(q.hasMessages()) {
            
            try {
              // Quickly pull the messages off the queue and re-release the lock
              List<ByteBuffer> cache = q.removeAll();
              IMessageParser<?> parser = mMessageParsers.get(msgId);
              
              if(parser == null) {
                mLog.error("Unable to find a parser for message type " + msgId + ".  Message(s) lost.");
              } else {
                // Now parse the messages in this thread, independently from how
                // often the messages come in
                for(ByteBuffer bb : cache) {
                  try {
                    mLog.debug("Parsing ", Arrays.toString(bb.array()));
                    Object o = parser.read(bb);
                    if(o != null && pType != null && pType.equals(o.getClass())) {
                      update(pType.cast(o));
                    }
                  } catch (Exception t) {
                    mLog.error("When parsing a message of type " + msgId);
                    mLog.exception(t);
                  }
                }
              }
            } catch (Exception e) {
              mLog.exception(e);
            }
          }
        }
        
        try {
          Thread.sleep(mDecodeRateMs);
        } catch (Exception e) {
          mLog.exception(e);
        }
      }
    });
    
    addParserForMessageType(EStaticMessageIds.BATCHED_MESSAGE.ordinal(), new BatchParser());
  }
  
  public void setReceiverDecodeRate(double pRateHz) {
//    mDecodeRateMs = (long)(1000d / pRateHz);
  }

  @Override
  public void addParserForMessageType(Integer pType, IMessageParser<?> pParser) {
    mMaxBufferSize = Math.max(mMaxBufferSize, pParser.getBufferSize() + 2*Integer.BYTES);
    mLog.info("Registering msg " + pType + " with buffer size " + pParser.getBufferSize() + " (max = " + mMaxBufferSize + ")");
    mMessageParsers.put(pType, pParser);
  }
  
  protected final void addMessageToQ(int pType, ByteBuffer pMsg) {
    try {
      MessageQueue queue = mMessageQ.get(pType);
      if(queue == null) {
        queue = new MessageQueue();
        synchronized(mMessageQ) {
          mMessageQ.put(pType, queue);
        }
      }
      
      queue.add(pMsg);
    } catch (InterruptedException e) {
      mLog.error("Socket thread was interrupted before its latest message could be " +
        "added to the queue.  Looks like we lost one.");
      mLog.exception(e);
    }
  }

  @Override
  public final void connect() {
    establishConnection();
    if(!mIsRegisteredWithShutdown) {
      Runtime.getRuntime().addShutdownHook(new Thread(() -> disconnect()));
      mIsRegisteredWithShutdown = true;
    }
  }

  @Override
  public void disconnect() {
    // noop by default
  }
  
  @Override
  public void setHostPort(int pPort) {
    mHostPort = pPort;
  }
  
  @Override
  public void setHostInfo(String pInfo) {
    mHostInfo = pInfo;
  }
  
  protected abstract void establishConnection();
  
  private final class BatchParser implements IMessageParser<ByteBuffer[]> {
    @Override
    public ByteBuffer[] read(ByteBuffer pData) {
      // Format:
      // 1 int : # of messages in the batch
      // 1-N messages: int (id), int (size), byte[] (body)
      int numMessages = pData.getInt();
      mLog.debug("Splitting " + numMessages + " batched messages");
      //NOTE - this doesn't duplicate bytes in memory.  These are merely
      // references to the array backing the original message, but with different
      // offsets & lengths representative of each individual message
      ByteBuffer[] result = new ByteBuffer[numMessages];
      for(int i = 0; i < numMessages; i++) {
        Integer msgId = pData.getInt();
        int msgSize = pData.getInt();
        mLog.debug("Found batched msgId " + msgId + " with body size " + msgSize);
//        ByteBuffer msgBody = ByteBuffer.allocate(msgSize);
        int offset = pData.position();
        ByteBuffer msgBody = ByteBuffer.wrap(pData.array(), offset, msgSize);
        pData.position(offset + msgSize);
        result[i] = msgBody;
        addMessageToQ(msgId, msgBody);
      }
      return result;
    }

    @Override
    public int getBufferSize() {
      return Protocols.MAX_PACKET_SIZE_BYTES; // Theoretical max TCP/UDP size packet.  Max for NT is unknown.
    }
    
  }
}
