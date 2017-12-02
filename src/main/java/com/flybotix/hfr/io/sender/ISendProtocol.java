package com.flybotix.hfr.io.sender;

import com.flybotix.hfr.io.ConnectionStatus;
import com.flybotix.hfr.util.lang.EnumUtils;
import com.flybotix.hfr.util.lang.IProvider;

public interface ISendProtocol extends IProvider<ConnectionStatus> {

  public void setDestAddress(String... pAddress);
  public void setDestPort(int pPort);
  public void setHostPort(int pPort);
  /**
   * NOTE - if this is called while a sender connection is live, then any
   * message currently being "batched" will be lost.
   * @param pUseBatching If <code>TRUE</code> then the protocol will batch multiple messages in a single packet if necessary
   */
  public void setBatching(boolean pUseBatching);
  public void connect();
  public void disconnect();
  public void sendMessage(int pId, byte[] pMessage);
  
  public default void sendMessage(String pId, byte[] pMessage) {
    sendMessage(pId.hashCode(), pMessage);
  }
  
  public default <E extends Enum<E>> void sendMessage(Class<E> pId, byte[] pMessage) {
    sendMessage(EnumUtils.hashOf(pId), pMessage);
  }
}
