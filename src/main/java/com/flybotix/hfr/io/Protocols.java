package com.flybotix.hfr.io;

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
