package com.flybotix.hfr.codex;

import com.flybotix.hfr.io.EConnectionState;
import com.flybotix.hfr.io.Protocols;
import com.flybotix.hfr.io.Protocols.EProtocol;
import com.flybotix.hfr.io.sender.ISendProtocol;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class CodexSender {
  private ISendProtocol mSender = null;
  private ILog mLog = Logger.createLog(CodexSender.class);
  private boolean mCanSend = false;
  
  /**
   * Creates a default CodexSender.  Note that any messages sent before
   * connecting (or init) will cause an <code>IllegalStateExcception</code>
   */
  public CodexSender() {
    
  }
  
  /**
   * Creates a CodexSender from a re-usable Send Protocol
   * @param pProtocol A previously-created and connected protocol
   */
  public CodexSender(ISendProtocol pProtocol) {
    this();
    mSender = pProtocol;
    mSender.addListener(status -> {
      if(status.getState() == EConnectionState.ERROR) {
        mLog.error(status);
        mCanSend = false;
      } else {
        mLog.info(status);
        mCanSend = true;
      }
    });
  }
  
  /**
   * If the sender protocol hasn't been initialized, this will initialize it using the input info
   * @param pType UDP, TCP, NetworkTables, or Passthrough (TCP and UDP only atm)
   * @param pHostPort The local port which the protocol will use to send from (Needed for TCP and UDP)
   * @param pDestPort The destination port of the receiver
   * @param pDestAddr The destination address of the receiver (IP Address or hostnames work.  URL's and FQDN's are untested.)
   */
  public void initConnection(EProtocol pType, int pHostPort, int pDestPort, String pDestAddr) {
    if(mSender == null) {
      mSender = Protocols.createSender(pType, pHostPort, pDestPort, pDestAddr);
    }
  }
  
  /**
   * Sends a codex using the previously-defined connection info.
   * @param pData The codex to send
   * @throws IllegalStateException If the connection has not been initialized
   * @param <V> The type backing the codex
   * @param <E> The enumeration backing the codex
   */
  public <V, E extends Enum<E> & CodexOf<V>> void send(Codex<V, E> pData) throws IllegalStateException{
    if(mSender == null) {
      throw new IllegalStateException("Cannot send a message since the comms" +
        " protocol hasn't been iniitialized.  Call initConnection() first.");
    }
    mSender.sendMessage(pData.msgId(), pData.encode());
  }
}
