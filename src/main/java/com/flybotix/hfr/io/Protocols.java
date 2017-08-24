package com.flybotix.hfr.io;

import java.util.Map;

import com.flybotix.hfr.io.receiver.ADataReceiver;
import com.flybotix.hfr.io.receiver.IMessageParser;
import com.flybotix.hfr.io.receiver.IReceiveProtocol;
import com.flybotix.hfr.io.receiver.TCPReceiver;
import com.flybotix.hfr.io.receiver.UDPReceiver;
import com.flybotix.hfr.io.sender.ISendProtocol;
import com.flybotix.hfr.io.sender.TCPSender;
import com.flybotix.hfr.io.sender.UDPSender;

public class Protocols {
  
  public static final int MAX_PACKET_SIZE_BYTES = 65507; // UDP datagram specification
  public static double MAX_PACKET_RATE_HZ = 50d;
  
  public enum EProtocol {
    TCP,
    UDP,
    NETWORK_TABLES,
    PASSHTHROUGH
  }
  
  @SuppressWarnings("rawtypes")
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
    if(result instanceof ADataReceiver) {
      ((ADataReceiver)result).setReceiverDecodeRate(MAX_PACKET_RATE_HZ);
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
      result.setBatching(true);
      break;
    case UDP:
      result = new UDPSender();
      result.setBatching(true);
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
