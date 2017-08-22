package com.flybotix.hfr.io.receiver;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

public class TCPReceiver extends ASocketReceiver {

  protected Socket mClientSocket;
  private ServerSocket mServerSocket;
  protected Semaphore mSocketLock = new Semaphore(1, true);

  private ILog mLog = Logger.createLog(TCPReceiver.class);

  private Map<Integer, byte[]> mMessageBuffers = new HashMap<>();
  
  @Override
  public void addParserForMessageType(Integer pType, IMessageParser<?> pParser) {
    mMessageParsers.put(pType, pParser);
    mLog.debug("Registering msg " + pType + " with buffer size " + pParser.getBufferSize());
    mMessageBuffers.put(pType, new byte[pParser.getBufferSize()]);
  }

  private void startReadThread(DataInputStream dis) {
    mConnectionThreads.execute(() -> {
      try {
        while (mStatus.isConnected() && !mClientSocket.isClosed()) {
          try {
            read(dis);
            update(mStatus.periodicUpdate(mClientSocket.isConnected()));
          } catch (Exception e) {
            if((e instanceof SocketException) && e.getMessage().contains("reset")){
              mLog.error(e.getMessage());
              mLog.error("Reconnecting...");
              disconnect();
              connect();
            } else {
              update(mStatus.unexpectedDisconnect());
              mLog.exception(e);
              break;
            }
          }
        }
      } catch (Exception e) {
        mLog.exception(e);
        update(mStatus.unexpectedDisconnect());
      }
    });
  }
  
  @Override public void disconnect() {
    super.disconnect();
    mSocketLock.release();// release mutex
    try {
      if (mClientSocket != null) {
        mClientSocket.close();
      }
    } catch (IOException e) {
      mLog.exception(e);
    }
  }

  @Override
  protected void establishConnection() {
    mConnectionThreads.execute(() -> {
      update(mStatus.attemptingConnection());
      try {
        mSocketLock.acquire();
        mServerSocket = new ServerSocket(mHostPort, 2, null);
        mLog.debug("Server Attempt Accept");
        mClientSocket = mServerSocket.accept();
        mLog.debug(mClientSocket);
        mLog.debug("Server Accept");
        mServerSocket.close();
        mLog.debug("Attempting to find Input Stream");
        DataInputStream dis = new DataInputStream(mClientSocket.getInputStream());
        mLog.debug("Input Stream Found");
        startReadThread(dis);
      } catch (IOException e) {
        mSocketLock.release();
        mLog.error("Error establishing server on port", mHostPort);
        mLog.exception(e);
        update(mStatus.errorDuringAttempt());
      } catch (InterruptedException e) {
        mLog.exception(e);
      }
      mLog.debug("Server Established");
      update(mStatus.connectionEstablished());
    });
  }

  /**
   * Reads a single message in its entirety from the socket. First it reads an int for message id,
   * then an int for message size, and then it continues to pull a quantity of bytes off the socket
   * equal to the message size. This method will block the socket thread until the whole message is
   * read or until the socket throws an Exception.
   * 
   * @param is
   * @throws Exception
   */
  private void read(DataInputStream is) throws Exception {
    int msgId = is.readInt();
    int msgSize = is.readInt();

    mLog.debug("int=", msgId, " hex=", Integer.toHexString(msgId), " length=", msgSize);
    byte[] buffer = mMessageBuffers.get(msgId);
    readInput(is, msgSize, buffer);
    mLog.debug(Arrays.toString(buffer));
    addMessageToQ(msgId, ByteBuffer.wrap(Arrays.copyOf(buffer, msgSize)));
  }

  /**
   * Assumes the socket is TCP and blocks on this method. Reads the available data from the socket.
   * If the only incoming data on the socket is less than the specified data length, this method
   * will loop and continue reading. This method only works for a TCP connection.
   * 
   * @param is
   * @param size
   *          length of the data
   * @param pBuffer
   *          the buffer to read into
   * @throws IOException
   *           if there was a socket error
   */
  private void readInput(DataInputStream is, int size, byte[] pBuffer) throws IOException {
    int amountRead = 0;
    int currentReadData = 0;
    while (amountRead < size) {
      currentReadData = is.read(pBuffer, amountRead, size - amountRead);
      if (currentReadData <= 0) {
        throw new IOException("Error - received no data when reading from the socket");
      } else {
        amountRead += currentReadData;
      }
    }
  }
}
