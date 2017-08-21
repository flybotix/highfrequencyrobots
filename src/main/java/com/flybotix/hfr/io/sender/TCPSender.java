package com.flybotix.hfr.io.sender;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class TCPSender extends ADataSender {
  protected Socket mClientSocket;
  protected Semaphore mSocketLock = new Semaphore(1, true);
  private DataOutputStream writer = null;
  
  private final Executor mThreads = Executors.newFixedThreadPool(2);

  private ILog mLog = Logger.createLog(TCPSender.class);
  
  @Override
  protected void establishConnection(InetAddress addr) {
    mThreads.execute(() -> {
      while(!mStatus.isConnected() && !mStatus.isIntentionallyDisconnected()) {
        try {
          mSocketLock.acquire();
          mLog.debug("Inet Addr:" + addr);
          mClientSocket = new Socket(addr, mDestPort);
          mLog.debug("Client Established");
    
          mLog.debug("Attempting to find Output Stream");
          try {
            OutputStream output = mClientSocket.getOutputStream();
            writer = new DataOutputStream (output);
            mLog.info("Data Output Stream Found");
          } catch (IOException e) {
            e.printStackTrace();
          } 
          update(mStatus.connectionEstablished());
          startReadTask();
        } catch (ConnectException ce) {
          mLog.error("Error Connecting to ", mDestAddress, ":", mDestPort);
          update(mStatus.errorDuringAttempt());
          mSocketLock.release();

          try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            mLog.exception(e);
          }
        } catch (IOException e) {
          mSocketLock.release();
          mLog.error("Error Connecting to ", mDestAddress, ":", mDestPort);
          mLog.exception(e);
          update(mStatus.errorDuringAttempt());
        } catch (InterruptedException e1) {
          mLog.exception(e1);
        }
      }
      mLog.warn("Finished connecting to remote.");
    });
  }

  private void startReadTask() {
    mThreads.execute(() -> {
      while(!mStatus.isIntentionallyDisconnected()) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        try {
          while (mStatus.isConnected() && !mClientSocket.isClosed()) {
            try {
              List<ByteBuffer> messages = mMessageQ.removeAll();
              for(ByteBuffer bb : messages) {
                mLog.debug("Writing message.");
                byte[] array = bb.array();
                mLog.debug(Arrays.toString(array));
                writer.write(array);
                writer.flush();
              }
            } catch (Exception e) {
              update(mStatus.unexpectedDisconnect());
              mLog.exception(e);
              break;
            }
            Thread.sleep(50);
          }
        } catch (Exception e) {
          mLog.exception(e);
          update(mStatus.unexpectedDisconnect());
        } finally {
          mSocketLock.release();// release mutex
          try {
            if (mClientSocket != null) {
              mClientSocket.close();
            }
          } catch (IOException e) {
            mLog.exception(e);
          }
        }
      }
    });
  }
}
