package com.flybotix.hfr.codex;

import com.flybotix.hfr.codex.encode.AEncoder;
import com.flybotix.hfr.io.EConnectionState;
import com.flybotix.hfr.io.Protocols;
import com.flybotix.hfr.io.Protocols.EProtocol;
import com.flybotix.hfr.io.sender.ISendProtocol;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class CodexSender <V, E extends Enum<E> & CodexOf<V>> {
  private ISendProtocol mSender = null;
  private final AEncoder<V, E> mEncoder;
  private ILog mLog = Logger.createLog(CodexSender.class);
  
  public CodexSender(Class<E> pEnum, boolean pUseCompression) {
    mEncoder = Codex.encoder.of(pEnum, pUseCompression);
  }
  
  public CodexSender<V,E> initConnection(EProtocol pType, int pHostPort, int pDestPort, String pDestAddr) {
    if(mSender == null) {
      mSender = Protocols.createSender(pType, pHostPort, pDestPort, pDestAddr);
      mSender.addListener(status -> {
        if(status.getState() == EConnectionState.ERROR) {
          mLog.error(status);
        } else {
          mLog.info(status);
        }
      });
    }
    return this;
  }
  
  public void send(Codex<V, E> pData) {
    if(mSender == null) {
      throw new IllegalStateException("Cannot send a message since the comms" +
        " protocol hasn't been iniitialized.  Call initConnection() first.");
    }
    mSender.sendMessage(mEncoder.getMsgId(), mEncoder.encode(pData));
  }
}
