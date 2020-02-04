package com.flybotix.hfr;

public class TestBoth {
  public static void main(String[] pArgs) throws Exception {
    System.out.println("Total available memory: " + Runtime.getRuntime().maxMemory()/1024d/1024d + " MB");
    TestClientReceiver.main(pArgs);
    TestRobotSender.main(pArgs);
  }
}
