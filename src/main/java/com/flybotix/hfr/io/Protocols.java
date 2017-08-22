package com.flybotix.hfr.io;

import java.util.Map;

import com.flybotix.hfr.io.receiver.IMessageParser;
import com.flybotix.hfr.io.receiver.IReceiveProtocol;
import com.flybotix.hfr.io.receiver.TCPReceiver;
import com.flybotix.hfr.io.receiver.UDPReceiver;
import com.flybotix.hfr.io.sender.ISendProtocol;
import com.flybotix.hfr.io.sender.TCPSender;
import com.flybotix.hfr.io.sender.UDPSender;

public class Protocols {
  public enum EProtocol {
    TCP,
    UDP,
    NETWORK_TABLES,
    PASSHTHROUGH
  }
  
  public static IReceiveProtocol createReceiver(EProtocol pType, int pHostPort, String pConnectionInfo, Map<Integer, IMessageParser<?>> pParsers) {
    IReceiveProtocol result = null;
    switch(pType) {
    case TCP:
      result = new TCPReceiver();
      break;
    case UDP:
      result = new UDPReceiver();
      break;
    default:
    }
    result.setHostPort(pHostPort);
    result.setHostInfo(pConnectionInfo);
    for(Integer id : pParsers.keySet()) {
      result.addParserForMessageType(id, pParsers.get(id));
    }
    result.connect();
    return result;
  }
  
  public static ISendProtocol createSender(EProtocol pType, int pHostPort, int pDestPort, String pDestAddr) {
    ISendProtocol result = null;
    switch(pType) {
    case TCP: 
      result = new TCPSender();
      break;
    case UDP:
      result = new UDPSender();
      break;
    default:
    }

    if(result != null) {
      result.setDestAddress(pDestAddr);
      result.setHostPort(pHostPort);
      result.setDestPort(pDestPort);
      result.connect();
    }
    
    return result;
  }
}
