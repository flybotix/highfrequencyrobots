package com.flybotix.hfr;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.flybotix.hfr.codex.Codex;
import com.flybotix.hfr.codex.CodexFactory;
import com.flybotix.hfr.codex.CodexOf;
import com.flybotix.hfr.codex.encode.AEncoder;

public class DataCoderTest {
  


  private static double nonsparse = 0.02;
  private static final double rate=100d;
  private static final double numIters = 240d * rate; // 4 minutes
  private static final double mem = TEST.values().length * numIters * Double.BYTES;
  private static final DecimalFormat df = new DecimalFormat("0.00");
  
  public static void main(String[] pArgs) {
    if(verifyCompressionAlgorithm()) {
      System.out.println();
      benchmark();
    }
  }
  
  private enum TEST implements CodexOf<Double>{
    A,B,C,D,E,
    F,G,H,I,J,
    K,L,M,N,O,
    P,Q,R,S,T,
    U,V,W,X,Y,Z,
    A1,B1,C1,D1,E1,F1,G1,H1,I1,J1,K1,L1,M1,N1,O1,P1,Q1,R1,S1,T1,U1,V1,W1,X1,Y1,Z1,
    A2,B2,C2,D2,E2,F2,G2,H2,I2,J2,K2,L2,M2,N2,O2,P2,Q2,R2,S2,T2,U2,V2,W2,X2,Y2,Z2,
    A3,B3,C3,D3,E3,F3,G3,H3,I3,J3,K3,L3,M3,N3,O3,P3,Q3,R3,S3,T3,U3,V3,W3,X3,Y3,Z3,
    A4,B4,C4,D4,E4,F4,G4,H4,I4,J4,K4,L4,M4,N4,O4,P4,Q4,R4,S4,T4,U4,V4,W4,X4,Y4,Z4,
    A5,B5,C5,D5,E5,F5,G5,H5,I5,J5,K5,L5,M5,N5,O5,P5,Q5,R5,S5,T5,U5,V5,W5,X5,Y5,Z5,
    A6,B6,C6,D6,E6,F6,G6,H6,I6,J6,K6,L6,M6,N6,O6,P6,Q6,R6,S6,T6,U6,V6,W6,X6,Y6,Z6,
    A7,B7,C7,D7,E7,F7,G7,H7,I7,J7,K7,L7,M7,N7,O7,P7,Q7,R7,S7,T7,U7,V7,W7,X7,Y7,Z7,
    A8,B8,C8,D8,E8,F8,G8,H8,I8,J8,K8,L8,M8,N8,O8,P8,Q8,R8,S8,T8,U8,V8,W8,X8,Y8,Z8,
    A9,B9,C9,D9,E9,F9,G9,H9,I9,J9,K9,L9,M9,N9,O9,P9,Q9,R9,S9,T9,U9,V9,W9,X9,Y9,Z9,
    A0,B0,C0,D0,E0,F0,G0,H0,I0,J0,K0,L0,M0,N0,O0,P0,Q0,R0,S0,T0,U0,V0,W0,X0,Y0,Z0
  }
  
  private static Stream<Double> getSparsityTestLoop() {
    List<Double> sparsity = new ArrayList<>();
    for(nonsparse = 0.02; nonsparse <= 1.0; nonsparse += 0.02) {
      sparsity.add(nonsparse);
    }
    return sparsity.stream();
  }
  
  private static Codex<Double, TEST> getRandomArray(double pProbOfData, AEncoder<Double, TEST> pCoder) {
    Codex<Double, TEST> res = new Codex<>(pCoder);
    for(TEST t : pCoder.getEnums()) {
      if(Math.random() <= pProbOfData) { 
        res.put(t, Math.random() * 1024d);
      }
    }
    return res;
  }
  
  private static boolean verifyCompressionAlgorithm() {
    System.out.println("Verifying Array Integrity.");
    AEncoder<Double, TEST> dc = CodexFactory.getDoubleEncoder(TEST.class, true);
    for(double s = 0.05; s < 1.0; s+= 0.05){
      nonsparse = s;
      Codex<Double, TEST> random = getRandomArray(nonsparse, dc);
      byte[] bytes = dc.encode(random);
      if(bytes.length > 0) {
        Codex<Double, TEST> decoded = dc.decode(ByteBuffer.wrap(bytes));
//        if(!Arrays.equals(random, decoded)) {
        System.out.println(random);
        System.out.println(decoded);
        if(!random.equals(decoded)) {
//          System.out.println(Arrays.toString(random));
//          System.out.println(Arrays.toString(bytes));
//          System.out.println(Arrays.toString(decoded));
          System.err.println("Arrays do not match.");
          return false;
        }
      }
    }
    return true;
  }
  
  private static void benchmark() {
    double packet = Double.BYTES * TEST.values().length;
    double bitset = Math.ceil(TEST.values().length / 8);
    double cpacket = packet + bitset;
    System.out.println("Testing " + TEST.values().length + " fields per packet/cycle.");
    System.out.println("Testing " + numIters + " total data packets.");
    System.out.println("Uncompressed packet size (kB): " + df.format(packet/1024d));
    System.out.println("Uncompressed bandwidth @"+rate+"hz (Mb/s): " + df.format(rate*packet*8d/1024d/1024d));
    System.out.println("BitSet compression metadata size (B): " + bitset );
    System.out.println("Max Compressed packet size (kB): " + df.format(cpacket/1024d));
    System.out.println("Max Compressed bandwidth @"+rate+"hz (Mb/s): " + df.format(rate*cpacket*8d/1024d/1024d));
    System.out.println("Total client memory footprint (MB): " + df.format(mem/1024d/1024d));
    System.out.println();
    
    System.out.println(Result.header());

    getSparsityTestLoop().forEach(s -> {
      nonsparse = s;
      System.gc();
      Result r = new Result();
      testRaw(r);
      testCompressed(r);
      System.out.println(r);
    });
  }
  
  private static void testRaw(Result r) {
//    AbstractEncoder<Double, TEST> dc = new UncompressedDoubleEncoder<>(TEST.class);
    AEncoder<Double, TEST> dc = CodexFactory.getDoubleEncoder(TEST.class, false);
    Map<Integer, Codex<Double, TEST>> input = new HashMap<>();
    for(Integer i = 0; i < numIters; i++) {
      input.put(i, getRandomArray(nonsparse, dc));
    }
    r.nonsparse = nonsparse;
    
    Map<Integer, Codex<Double, TEST>> output = new HashMap<>();
    Map<Integer, byte[]> transmitted = new HashMap<>();
    
    long start = System.nanoTime();
    
    for(Integer i : input.keySet()) {
      transmitted.put(i, dc.encode(input.get(i)));
    }
    long encode = System.nanoTime();
    
    for(Integer idx : transmitted.keySet()) {
      byte[] incoming = transmitted.get(idx);
      if(incoming.length > 0) {
        output.put(idx, dc.decode(ByteBuffer.wrap(incoming)));
      }
    }
    long decode = System.nanoTime();
    
    r.uEncodeNs = encode - start;
    r.uDecodeNs = decode - encode;
    r.uTotalNs = decode - start;

    double size = 0;
    for(Integer mint : input.keySet()) {
      size += transmitted.get(mint).length;
    }
    r.uSizeB = size;
  
  }
  
  private static void testCompressed(Result r) {
    AEncoder<Double, TEST> dc = CodexFactory.getDoubleEncoder(TEST.class, true);
    Map<Integer, Codex<Double, TEST>> input = new HashMap<>();
    for(Integer i = 0; i < numIters; i++) {
      input.put(i, getRandomArray(nonsparse, dc));
    }
    r.nonsparse = nonsparse;

    Map<Integer, Codex<Double, TEST>> output = new HashMap<>();
    Map<Integer, byte[]> transmitted = new HashMap<>();
    
    long start = System.nanoTime();
    
    for(Integer i : input.keySet()) {
      transmitted.put(i, dc.encode(input.get(i)));
    }
    long encode = System.nanoTime();
    
    for(Integer idx : transmitted.keySet()) {
      byte[] b = transmitted.get(idx);
      output.put(idx, dc.decode(ByteBuffer.wrap(b)));
    }
    long decode = System.nanoTime();
    
    r.cEncodeNs = encode - start;
    r.cDecodeNs = decode - encode;
    r.cTotalNs = decode - start;
    double size = 0;
    for(Integer mint : input.keySet()) {
      size += transmitted.get(mint).length;
    }
    r.cSizeB = size;
  }
  

  private static class Result {
    double nonsparse = 0d;
    double cEncodeNs = 0d;
    double cDecodeNs = 0d;
    double cTotalNs = 0d;
    double cSizeB = 0d;
    double uEncodeNs = 0d;
    double uDecodeNs = 0d;
    double uTotalNs = 0d;
    double uSizeB = 0d;
    
    public String toString() {
      return new StringBuilder()
        .append(df.format((1-this.nonsparse)*100d))
        .append('\t')
        .append(df.format(cEncodeNs /numIters/1000d))
        .append('\t')
        .append(df.format(cDecodeNs/numIters/1000d))
        .append('\t')
        .append(df.format(cTotalNs/1000d/1000d))
        .append('\t')
        .append(df.format(cSizeB/1024d/1024d))
        .append('\t')
        .append(df.format(uEncodeNs/numIters/1000d))
        .append('\t')
        .append(df.format(uDecodeNs/numIters/1000d))
        .append('\t')
        .append(df.format(uTotalNs/1000d/1000d))
        .append('\t')
        .append(df.format(uSizeB/1024d/1024d))
        .toString();
    }

    public static String header() {
      return new StringBuilder()
        .append("% Sparse")
        .append('\t')
        .append("cEncode (us)")
        .append('\t')
        .append("cDecode (us)")
        .append('\t')
        .append("cTotal (ms)")
        .append('\t')
        .append("cSize (MB)")
        .append('\t')
        .append("uEncode (us)")
        .append('\t')
        .append("uDecode (us)")
        .append('\t')
        .append("uTotal (ms)")
        .append('\t')
        .append("uSize (MB)")
        .toString();
    }
  }
}
