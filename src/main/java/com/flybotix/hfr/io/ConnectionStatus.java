package com.flybotix.hfr.io;

import java.time.Instant;

public class ConnectionStatus {
  EConnectionState mState = EConnectionState.DISCONNECTED;
  boolean mIsConnected = false;
  boolean mIsIntentionallyDisconnected = false;
  private Instant mFirstConnectionAttempt = null;
  private Instant mLatestConnectionAttempt = null;
  private Instant mConnectionEstablishedAt = null;
  private Instant mLatestConnectionHeartbeat = null;
  private Instant mLatestDisconnect = null;
  
  public String toString() {
    return mState.name();
  }
  
  public EConnectionState getState() {
    return mState;
  }
  
  public boolean isConnected() {
    return mIsConnected;
  }
  
  public boolean isAttempting() {
    return mState == EConnectionState.ATTEMPTING;
  }
  
  public boolean isIntentionallyDisconnected() {
    return mIsIntentionallyDisconnected;
  }
  
  public Instant getFirstConnectionAttempt() {
    return mFirstConnectionAttempt;
  }
  
  public Instant getLatestConnectionAttempt() {
    return mLatestConnectionAttempt;
  }
  
  public Instant getConnectionEstablishedAt() {
    return mConnectionEstablishedAt;
  }

  public Instant getLatestConnectionHeartbeat() {
    return mLatestConnectionHeartbeat;
  }

  public Instant getLatestDisconnect() {
    return mLatestDisconnect;
  }

  public ConnectionStatus attemptingConnection() {
    mIsConnected = false;
    mState = EConnectionState.ATTEMPTING;
    if(mFirstConnectionAttempt == null) {
      mFirstConnectionAttempt = Instant.now();
    }
    mLatestConnectionAttempt = Instant.now();
    return this;
  }

  public ConnectionStatus errorDuringAttempt() {
    mIsConnected = false;
    mState = EConnectionState.ERROR;
    return this;
  }

  public ConnectionStatus connectionEstablished() {
    mIsConnected = true;
    mIsIntentionallyDisconnected = false;
    mState = EConnectionState.ESTABLISHED;
    mConnectionEstablishedAt = Instant.now();
    return this;
  }
  
  public ConnectionStatus unexpectedDisconnect() {
    mIsConnected = false;
    mState = EConnectionState.ERROR;
    mLatestDisconnect = Instant.now();
    return this;
  }
  
  public ConnectionStatus expectedDisconnect() {
    mIsConnected = false;
    mIsIntentionallyDisconnected = true;
    mState = EConnectionState.DISCONNECTED;
    mLatestDisconnect = Instant.now();
    return this;
  }

  public ConnectionStatus periodicUpdate(boolean connected) {
    if(connected) {
      mIsConnected = true;
      mState = EConnectionState.ESTABLISHED;
      mLatestConnectionHeartbeat = Instant.now();
    } else {
      return unexpectedDisconnect();
    }
    return this;
  }
}
