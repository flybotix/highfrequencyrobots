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
  
  public CodexSender() {
    
  }
  
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
  
  public void initConnection(EProtocol pType, int pHostPort, int pDestPort, String pDestAddr) {
    if(mSender == null) {
      mSender = Protocols.createSender(pType, pHostPort, pDestPort, pDestAddr);
    }
  }
  
  public <V, E extends Enum<E> & CodexOf<V>> void send(Codex<V, E> pData) {
    if(mSender == null) {
      throw new IllegalStateException("Cannot send a message since the comms" +
        " protocol hasn't been iniitialized.  Call initConnection() first.");
    }
    mSender.sendMessage(pData.msgId(), pData.encode());
  }
}
