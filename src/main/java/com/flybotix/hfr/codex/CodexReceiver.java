package com.flybotix.hfr.codex;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.flybotix.hfr.codex.encode.AEncoder;
import com.flybotix.hfr.io.Protocols;
import com.flybotix.hfr.io.Protocols.EProtocol;
import com.flybotix.hfr.io.receiver.IMessageParser;
import com.flybotix.hfr.io.receiver.IReceiveProtocol;
import com.flybotix.hfr.util.lang.Delegator;

/**
 * Protocol-agnostic receiver that parses codex messages. This presumes that each
 * message is chunked, one Codex per receipt.
 * 
 * In FRC, this is useful for NetworkTables implementations where
 * one set() on the robot side equates to exactly one message
 * (i.e. unbatched).  This is simple, and should work "well enough"
 * out of the box for most teams.  For the rest of us, a batching
 * strategy will have to be implemented in the network code, and that batching code can
 * then use multiple calls to this class per message.
 * 
 * Note that this will also work for both TCP and UDP socket clients, if the clients
 * handle message type and message size.
 *
 * @param <E>
 * @param <V>
 */
public class CodexReceiver<V, E extends Enum<E> & CodexOf<V>> extends Delegator<Codex<V, E>> implements IMessageParser<Codex<V, E>> {

  protected final AEncoder<V, E> mEncoder;
  protected IReceiveProtocol mReceiveProtocol = null;
  
  public CodexReceiver(AEncoder<V, E> pEncoder) {
    mEncoder = pEncoder;
  }
  
  public CodexReceiver(Class<E> pEnum) {
    this(Codex.encoder.of(pEnum, true));
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
  
  public IReceiveProtocol startReceiving(EProtocol pType, int pHostPort, String pConnectionInfo) {
    Map<Integer, IMessageParser<?>> parser = new HashMap<>();
    parser.put(mEncoder.getMsgId(), this);
    mReceiveProtocol = Protocols.createReceiver(pType, pHostPort, pConnectionInfo, parser);
    return mReceiveProtocol;
  }
}
