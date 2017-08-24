package com.flybotix.hfr;

import com.flybotix.hfr.io.Protocols.EProtocol;

interface TestConfig {
  static final double MAX_PACKET_RATE_HZ = 20;
  
  static final double     TEST_HIGH_FREQUENCY_DATA_RATE_HZ = 100d;
  static final double     TEST_HIGH_FREQUENCY_DATA_RATE_RAND = 0.05d;
  static final boolean    TEST_HIGH_FREQUENCY_DATA_OVER_SOCKET = true;
  static final int        TEST_HIGH_FREQUENCY_DATA_NUM_SEND_THREADS = 1;
  
  static final EProtocol  TEST_SOCKET_PROTOCOL = EProtocol.UDP;
  static final int        TEST_RECEIVER_PORT = 7777;
  static final int        TEST_SENDER_PORT = 7778;
  static final String     TEST_RECEIVER_HOST_NAME = "localhost";
}
