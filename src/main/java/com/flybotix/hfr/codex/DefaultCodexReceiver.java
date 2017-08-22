package com.flybotix.hfr.codex;

import java.nio.ByteBuffer;

import com.flybotix.hfr.codex.encode.AEncoder;
import com.flybotix.hfr.io.receiver.IMessageParser;
import com.flybotix.hfr.util.lang.Delegator;

/**
 * Protocol-agnostic receiver that parses codex messages. This presumes that each
 * message is chunked, one per receipt.
 * 
 * In FRC, this is useful for NetworkTables implementations where
 * one set() on the robot side equates to exactly one message
 * (i.e. unbatched).  This is simple, and should work "well enough"
 * out of the box for most teams.  For the rest of us, a batching
 * strategy will have to be implemented, and that batching code can
 * then use multiple calls to this class per message.
 * 
 * Note that this will also work for both TCP & UDP socket clients, if the clients
 * handle message type & message size.
 *
 * @param <E>
 * @param <V>
 */
public class DefaultCodexReceiver<V, E extends Enum<E> & Type<V>> extends Delegator<Codex<V, E>> implements IMessageParser<Codex<V, E>> {

  protected final AEncoder<V, E> mEncoder;

  public DefaultCodexReceiver(AEncoder<V, E> pEncoder) {
    mEncoder = pEncoder;
  }
  
  @Override
  public Codex<V, E> read(ByteBuffer pData) {
    Codex<V, E> codex = mEncoder.decode(pData);
    update(codex);
    return codex;
  }

  @Override
  public int getBufferSize() {
    return mEncoder.getBufferSizeInBytes(); 
  }
}
