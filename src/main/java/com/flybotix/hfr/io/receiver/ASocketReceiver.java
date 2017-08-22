package com.flybotix.hfr.io.receiver;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.flybotix.hfr.io.ConnectionStatus;
import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public abstract class ASocketReceiver extends ADataReceiver<ConnectionStatus> {
  
  protected final ConnectionStatus mStatus;
  private ILog mLog = Logger.createLog(ASocketReceiver.class);
  protected final Executor mConnectionThreads = Executors.newFixedThreadPool(2);
  
  public ASocketReceiver() {
    super(ConnectionStatus.class);
    mStatus = new ConnectionStatus();
  }
  
  @Override
  public void disconnect() {
    update(mStatus.expectedDisconnect());
  }
}
