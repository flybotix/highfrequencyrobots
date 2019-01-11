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

public class MessageProtocols {
  
  public static final int MAX_PACKET_SIZE_BYTES = 65507; // UDP datagram specification
  public static double MAX_PACKET_RATE_HZ = 50d;
  public static final String NT_ELEMENT_NAME = MessageProtocols.class.getPackage().getName() + "." + MessageProtocols.class.getName();
  
  public enum EProtocol {
    TCP,
    UDP,
//    NETWORK_TABLES,
    PASSHTHROUGH
  }
  
  /**
   * Creates and connects a receiver using the provided information.  If the protocol type is TCP, then
   * this method will block until a client connects.  No matter the protocol, it is recommended to create
   * a receiver in a separate thread.
   * @param pType UDP, TCP, NetworkTables, or Passthrough (Only TCP and UDP work at the moment)
   * @param pHostPort The port that will open up on the server in order to accept incoming meessages
   * @param pConnectionInfo Protocol-specific info.  Most of the time this can be blank, but it's necessary for NT.
   * @return an interface into the protocol
   */
  @SuppressWarnings("rawtypes")
  public static IReceiveProtocol createReceiver(EProtocol pType, int pHostPort, String pConnectionInfo) {
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
    result.connect();
    return result;
  }
  
  /**
   * Creates and connects a receiver using the provided information, then adds all of the input parsers to the receiver.
   * If the protocol type is TCP, then
   * this method will block until a client connects.  No matter the protocol, it is recommended to create
   * a receiver in a separate thread.
   * @param pType UDP, TCP, NetworkTables, or Passthrough (Only TCP and UDP work at the moment)
   * @param pHostPort The port that will open up on the server in order to accept incoming meessages
   * @param pConnectionInfo Protocol-specific info.  Most of the time this can be blank, but it's necessary for NT.
   * @param pParsers Map of parsers to add to the receiver
   * @return an interface into the protocol
   */
  public static IReceiveProtocol createReceiver(EProtocol pType, int pHostPort, String pConnectionInfo, Map<Integer, IMessageParser<?>> pParsers) {
    IReceiveProtocol result = createReceiver(pType, pHostPort, pConnectionInfo);
    for(Integer id : pParsers.keySet()) {
      result.addParserForMessageType(id, pParsers.get(id));
    }
    return result;
  }
  
  /**
   * Creates a send protocol based upon the Protocol type.  This thread does not block (even for TCP) since
   * the receivers handle connections in their own thread pool.  There are no guarantees that messages
   * sent prior to an actual connection (i.e. TCP) will be received by the other side.
   * <br><br>
   * Also note that for the time being, TCP and UDP protocols will use batching. 
   * 
   * @param pType UDP, TCP, NetworkTables, or Passthrough.  Currently, only TCP and UDP are supported.
   * @param pHostPort The port that will be bound to in order to send data.
   * @param pDestPort The destination port of the receiver.
   * @param pDestAddr The destination address.  Accepts hostname or IP address.
   * @return an interface to the protocol
   */
  public static ISendProtocol createSender(EProtocol pType, int pHostPort, int pDestPort, String pDestAddr) {
    return createSender(pType, pHostPort, pDestPort, pDestAddr);
  }
  
  /**
   * Creates a send protocol based upon the Protocol type.  This thread does not block (even for TCP) since
   * the receivers handle connections in their own thread pool.  There are no guarantees that messages
   * sent prior to an actual connection (i.e. TCP) will be received by the other side.
   * <br><br>
   * Also note that for the time being, TCP and UDP protocols will use batching. 
   * 
   * @param pType UDP, TCP, NetworkTables, or Passthrough.  Currently, only TCP and UDP are supported.
   * @param pHostPort The port that will be bound to in order to send data.
   * @param pDestPort The destination port of the receiver.
   * @param pDestAddresses The destination address.  Accepts hostname or IP address.  The API will run through each host/ip
   * in the array and connect to the first one that's available.
   * @return an interface to the protocol
   */
  public static ISendProtocol createSender(EProtocol pType, int pHostPort, int pDestPort, String... pDestAddresses) {
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
//    case NETWORK_TABLES: 
//      result = new NTSender();
//      break;
    default:
    }

    if(result != null) {
      result.setDestAddress(pDestAddresses);
      result.setHostPort(pHostPort);
      result.setDestPort(pDestPort);
      result.connect();
    }
    
    return result;
  }

//  public static ISendProtocol createNTSender(String pTableName) {
//    return createSender(EProtocol.NETWORK_TABLES, -1, -1, pTableName);
//  }
  
}
