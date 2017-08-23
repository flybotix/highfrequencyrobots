package com.flybotix.hfr.io;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public final class MessageQueue {
  protected final LinkedList<ByteBuffer> Q = new LinkedList<>();
  protected final Semaphore sync = new Semaphore(1, true);
  
  public boolean hasMessages() {
    return !Q.isEmpty();
  }
  
  public List<ByteBuffer> removeAllMessageUpToSize(int pMaxSize) throws InterruptedException {
    List<ByteBuffer> result = new LinkedList<>();
    sync.acquire();
    int currentSize = 0;
    boolean canAddMore = true;
    while(canAddMore && !Q.isEmpty()) {
      ByteBuffer bb = Q.removeFirst();
      if(currentSize + bb.limit() > pMaxSize) {
        canAddMore = false;
        Q.addFirst(bb);
      } else {
        result.add(bb);
        currentSize += bb.limit();
      }
    }
    sync.release();
    return result;
  }
  
  public int getAllMessagesSize() throws InterruptedException {
    int result = 0;
    sync.acquire();
    result += getTotalMessageSize(Q);
    sync.release();
    return result;
  }
  
  public static int getTotalMessageSize(List<ByteBuffer> pBuffers) {
    int result = 0;
    for(int i = 0; i < pBuffers.size(); i++) {
      ByteBuffer bb = pBuffers.get(i);
      if(bb != null) {
        result += bb.limit();
      }
    }
    return result;
  }
  
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

  public boolean isEmpty() throws InterruptedException {
    boolean result = false;
    sync.acquire();
    result = Q.isEmpty();
    sync.release();
    return result;
  }
}