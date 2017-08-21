package com.flybotix.hfr.io.receiver;

public interface IDataReceiver extends Runnable {
  public void addParserForMessageType(Integer pType, IMessageParser<?> pParser);
  
  public default void addParserForMessageType(Class<?> pType, IMessageParser<?> pParser) {
    addParserForMessageType(pType.hashCode(), pParser);
  }
  
  public default void addParserForMessageType(String pType, IMessageParser<?> pParser) {
    addParserForMessageType(pType.hashCode(), pParser);
  }
  
  public void disconnect();
  
  public void connect();
}
