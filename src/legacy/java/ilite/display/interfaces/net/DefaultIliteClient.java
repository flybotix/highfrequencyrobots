package ilite.display.interfaces.net;

import static ilite.display.interfaces.net.EConnectionState.ATTEMPTING;
import static ilite.display.interfaces.net.EConnectionState.DISCONNECTED;
import static ilite.display.interfaces.net.EConnectionState.ESTABLISHED;
import ilite.util.logging.ILog;
import ilite.util.logging.Logger;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultIliteClient extends TCPClient
implements IRobotConnector
{
  // *** Default Robot Connection
  public static final String sDEFAULT_ROBOT_IP = "10.18.85.2";
  public static final int sDEFAULT_ROBOT_PORT = 1180;
  public static final int sDEFAULT_RETRY_ATTEMPTS = 3;
  public static final int sDEFAULT_RETRY_SLEEP = 5000;
  
  public static final int scDataElementLength = 4;
  
  // *** Buffering elements
  byte[] maHeaderDataRegion = new byte[scDataElementLength];
  byte[] maMsgSizeDataRegion = new byte[scDataElementLength];
  byte[] maMessageDataRegion = new byte[scDataElementLength*128*1024];
  int messageSize = 0;
  int dataType = 0;
  private EConnectionState mCurrentConnectionState = EConnectionState.DISCONNECTED;
  
  private AtomicBoolean mCanConnect = new AtomicBoolean(false);

  // *** Actual Members
  private Map<Integer, AbstractMagicNumberMsgDecoder<?>> mDecoders = new HashMap<>();
  private ILog mLog = Logger.createLog(DefaultIliteClient.class);
  
  @Override
  public final void parse(InputStream is) throws Exception
  {
    readInput (is, scDataElementLength, maHeaderDataRegion);
    readInput (is, scDataElementLength, maMsgSizeDataRegion);
   
    messageSize = ByteBuffer.wrap(maMsgSizeDataRegion).getInt();        
    dataType = ByteBuffer.wrap(maHeaderDataRegion).getInt();

    mLog.debug("int=",dataType," hex=", Integer.toHexString(dataType), " length=", messageSize);
    readInput (is, messageSize, maMessageDataRegion);
    
    AbstractMagicNumberMsgDecoder<?> decoder = mDecoders.get(dataType);
    if(decoder != null)
    {
      decoder.decode(maMessageDataRegion, messageSize);
    }
    else
    {
      mLog.warn("Couldn't decode message type" + 
          Integer.toString(dataType) + " / 0x" + 
          Integer.toHexString(dataType));
    }
  }
  
  /**
   * Adds a parser for a magic number to the socket listener
   * @param pDecoder
   */
  public final <T> void addParser(AbstractMagicNumberMsgDecoder<T> pDecoder)
  {
    mDecoders.put(pDecoder.getMagicNumber(), pDecoder);
  }

  /**
   * Disconnects from client socket
   */
  @Override
  public void disconnectRobot()
  {
    mLog.info("Disconnecting");
    if(isConnected())
    {
      disconnect();
    }
    mCanConnect.set(false);
    
    mCurrentConnectionState = DISCONNECTED;
  }

  
  /**
   * Connects using the default IP & port
   */
  @Override
  public void attemptRobotConnection()
  {
    connect(
        sDEFAULT_ROBOT_IP, 
        sDEFAULT_ROBOT_PORT, 
        sDEFAULT_RETRY_ATTEMPTS, 
        sDEFAULT_RETRY_SLEEP);
  }
  
  /**
   * Connects to the client using the specified IP & Port
   */
  public void connect(final String pIPAddr, final int pPort, final int pRetryAttempts, final int pRetryInterval)
  {
    mLog.warn("Connecting To ", pIPAddr, ":", pPort, " RETRY=", pRetryAttempts, "x", pRetryInterval);
    mCanConnect.set(true);
    Runnable r = new Runnable()
    {      
      @Override
      public void run()
      {
        for(int i = 0; i < pRetryAttempts; i++)
        {
          if(mCanConnect.get())
          {
            mCurrentConnectionState = ATTEMPTING;
            mCurrentConnectionState = connectToClient(pIPAddr, pPort);
            if(mCurrentConnectionState == ESTABLISHED)
            {
              postConnectionInit();
              break;
            }
            else
            {
              try{Thread.sleep(pRetryInterval);} catch (InterruptedException e){}
            }
          }
          else
          {
            break;
          }
        } 
        
        if(isConnected())
        {
          while(mCanConnect.get())
          {
            DefaultIliteClient.this.run();
          }
          
          //If we get here, then the socket was closed.
          postConnectionClose();
        }
      }
    };
    Thread t1 = new Thread(r , "Client Connect Thread");
    t1.start();
  }

  /**
   * Override this to receive notification that the socket has connected
   */
  protected void postConnectionInit()
  {
    
  }

  /**
   * Override this to receive notification that the socket has closed
   */
  protected void postConnectionClose()
  {
    
  }

  /**
   * @return TCPClient's connection state
   */
  public EConnectionState getConnectionState()
  {
    return mCurrentConnectionState;
  }
}
