package com.flybotix.hfr.io;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public final class MessageQueue {
  private final LinkedList<ByteBuffer> Q = new LinkedList<>();
  private final Semaphore sync = new Semaphore(1, true);
  
  public void add(ByteBuffer bb) throws InterruptedException {
    sync.acquire();
    Q.addLast(bb);
    sync.release();
  }
  
  public void addFirst(ByteBuffer bb) throws InterruptedException {
    sync.acquire();
    Q.addFirst(bb);
    sync.release();
  }
  
  public ByteBuffer removeFirst() throws InterruptedException {
    ByteBuffer result = null;
    sync.acquire();
    if(!Q.isEmpty()) {
      result = Q.removeFirst();
    }
    sync.release();
    return result;
  }
  
  public List<ByteBuffer> removeAll() throws InterruptedException {
    List<ByteBuffer> result = new ArrayList<>();
    sync.acquire();
    while(!Q.isEmpty()) {
      result.add(Q.removeFirst());
    }
    sync.release();
    return result;
  }
}