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
 * @param <V> The type backing the codex
 * @param <E> The enumeration backing the codex
 */
public class CodexReceiver<V, E extends Enum<E> & CodexOf<V>> extends Delegator<Codex<V, E>> implements IMessageParser<Codex<V, E>> {

  protected final AEncoder<V, E> mEncoder;
  protected IReceiveProtocol mReceiveProtocol = null;
  
  /**
   * Creates a receiver using the input encoder.  This encoder is necessary in order to
   * decode a message from byte[] to a Codex
   * @param pEncoder Codex encoder
   */
  public CodexReceiver(AEncoder<V, E> pEncoder) {
    mEncoder = pEncoder;
  }
  
  /**
   * Creates a receiver using the input encoder and a re-usable receiver protocol.  The
   * encoder is necessary to decode a message from byte[] to Codex.  This contrustor will
   * simply register with the receiver protocol and will not attempt connections.
   * @param pEncoder Codex encoder
   * @param pReceiver Reusable receiver protocol
   */
  public CodexReceiver(AEncoder<V, E> pEncoder, IReceiveProtocol pReceiver) {
    this(pEncoder);
    mReceiveProtocol = pReceiver;
    mReceiveProtocol.addParserForMessageType(mEncoder.getMsgId(), this);
  }
  
  /**
   * USE THIS CONTRUCTOR BY DEFAULT
   * Creates a receiver using the default encoder for the codex type and the resuable protocol.
   * @param pEnum The enum backing the codex
   * @param pReceiver The reusabled receiver protocol
   */
  public CodexReceiver(Class<E> pEnum, IReceiveProtocol pReceiver) {
    this(Codex.encoder.of(pEnum));
    mReceiveProtocol = pReceiver;
    mReceiveProtocol.addParserForMessageType(mEncoder.getMsgId(), this);
  }
  
  /**
   * Creates a receiver using the default encoder for the codex type.  Does not auto-connect,
   * so nothing will happen until the startReceiving() method is called.
   * @param pEnum The enumeration backing the codex
   */
  public CodexReceiver(Class<E> pEnum) {
    this(Codex.encoder.of(pEnum));
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
  
  /**
   * Initializes a connection using the input parameters.
   * @param pType TCP, UDP, NetworkTables, or passthrough
   * @param pHostPort The port on the local machine to open up
   * @param pConnectionInfo Any additional connection info
   * @return An interface to a reusable receive protocol
   */
  public IReceiveProtocol startReceiving(EProtocol pType, int pHostPort, String pConnectionInfo) {
    Map<Integer, IMessageParser<?>> parser = new HashMap<>();
    parser.put(mEncoder.getMsgId(), this);
    mReceiveProtocol = Protocols.createReceiver(pType, pHostPort, pConnectionInfo, parser);
    return mReceiveProtocol;
  }
}
