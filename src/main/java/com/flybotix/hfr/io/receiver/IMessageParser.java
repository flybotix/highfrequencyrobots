package com.flybotix.hfr.io.receiver;

import java.nio.ByteBuffer;

public interface IMessageParser<T> {
  /**
   * Read the message from the buffer.  Note - this bytebuffer may have already been
   * offset in order to account for different protocols, so its current position
   * should be the start of the header (if this message type has one) or body (if
   * this message type doesn't have a header).
   * 
   * @param pData
   */
  public T read(ByteBuffer pData);
  
  /**
   * @return the size of the header + the size of the body of the message
   */
  public int getBufferSize();
}
