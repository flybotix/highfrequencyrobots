package com.flybotix.hfr;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkIO {
  
  
  public static void main(String[] pArgs) throws SocketException {
    Enumeration<NetworkInterface> connections = NetworkInterface.getNetworkInterfaces();
    
    while(connections.hasMoreElements()) {
      NetworkInterface ni = connections.nextElement();
      Enumeration<InetAddress> addrs = ni.getInetAddresses();
      while(addrs.hasMoreElements()) {
        InetAddress addr = addrs.nextElement();
        if(addr.isLoopbackAddress() == false && addr instanceof Inet4Address) {
          System.out.println(addr.getHostAddress());
        }
      }
    }
  }
}
