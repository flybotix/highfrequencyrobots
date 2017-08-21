package com.flybotix.hfr.io.sender;

import com.flybotix.hfr.io.ConnectionStatus;
import com.flybotix.hfr.util.lang.IProvider;

public interface ISendProtocol extends IProvider<ConnectionStatus> {

  public void setDestAddress(String pAddress);
  public void setDestPort(int pPort);
  public void setHostPort(int pPort);
  public void connect();
  public void disconnect();
  public void sendMessage(int pId, byte[] pMessage);
  
  public default void sendMessage(String pId, byte[] pMessage) {
    sendMessage(pId.hashCode(), pMessage);
  }
  
  public default void sendMessage(Class<?> pId, byte[] pMessage) {
    sendMessage(pId.hashCode(), pMessage);
  }
}
