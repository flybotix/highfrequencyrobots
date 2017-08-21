package com.flybotix.hfr.io.receiver;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.flybotix.hfr.io.ConnectionStatus;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public abstract class ASocketReceiver extends AMessageReceiver<ConnectionStatus> {
  
  public enum ESocketType {
    SERVER,
    CLIENT,
    NEITHER
  }
  
  protected final ESocketType mType;
  protected final ConnectionStatus mStatus;
  private ILog mLog = Logger.createLog(ASocketReceiver.class);
  protected int mHostPort;
  protected String mHostAddress = "localhost";
  protected final Executor mThreads = Executors.newFixedThreadPool(2);
  
  public ASocketReceiver(ESocketType pType) {
    super(ConnectionStatus.class);
    mType = pType;
    mStatus = new ConnectionStatus(mType);
  }
  
  public void setPort(int pPort) {
    mHostPort = pPort;
  }
  
  public void setIpAddress(String pAddress) {
    mHostAddress = pAddress;
  }
  
  @Override
  public void disconnect() {
    update(mStatus.expectedDisconnect());
  }
}
