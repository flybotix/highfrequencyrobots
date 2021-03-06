package com.flybotix.hfr.io.receiver;

public interface IReceiveProtocol {
  public void addParserForMessageType(Integer pType, IMessageParser<?> pParser);
  
  public default void addParserForMessageType(Class<?> pType, IMessageParser<?> pParser) {
    addParserForMessageType(pType.hashCode(), pParser);
  }
  
  public default void addParserForMessageType(String pType, IMessageParser<?> pParser) {
    addParserForMessageType(pType.hashCode(), pParser);
  }
  
  public void setHostPort(int pPort);
  
  public void setHostInfo(String pHostInfo);
  
  public void disconnect();
  
  public void connect();
}
