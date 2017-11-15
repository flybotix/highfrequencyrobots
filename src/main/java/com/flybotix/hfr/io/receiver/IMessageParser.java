package com.flybotix.hfr.io.receiver;

import java.nio.ByteBuffer;

/**
 * @param <T> The type that should be returned by the parser
 */
public interface IMessageParser<T> {
  /**
   * Read the message from the buffer.  Do something with the message while you have it.
   * 
   * Note - this bytebuffer may have already been
   * offset in order to account for different protocols, so its current position
   * should be the start of the header (if this message type has one) or body (if
   * this message type doesn't have a header).  If custom-positioning is needed,
   * store the buffer's position before any buffer reads occur.
   * 
   * @param pData The buffer data to decode
   * @return The decoded data
   */
  public T read(ByteBuffer pData);
  
  /**
   * @return the size of the header + the size of the body of the message
   */
  public int getBufferSize();
}
