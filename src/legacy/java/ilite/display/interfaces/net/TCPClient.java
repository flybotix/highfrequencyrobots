package ilite.display.interfaces.net;

import ilite.util.logging.ILog;
import ilite.util.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;


public abstract class TCPClient implements Runnable, IMessageParser
{
	Socket mClientSocket;
	ServerSocket mServerSocket;
	public Semaphore mcClientConnected;
	private final ILog mLog = Logger.createLog(TCPClient.class);

	private boolean mConnectionStatus;

	public boolean isConnected(){ return mConnectionStatus; }

	@Override
  public void run()
	{
	  try
	  {
	    mLog.debug("Attempting to find Input Stream");
	    InputStream lnDataFromServer = null;
	    try {
	      lnDataFromServer = mClientSocket.getInputStream();
	      mLog.debug("Input Stream Found");
	    } catch (IOException e) {
	      e.printStackTrace();
	    }//create reader to read data from client

	    while(!mClientSocket.isClosed()) {
	      try {
	        parse(lnDataFromServer);
	      } catch (Exception e) {
	        mLog.exception(e);
	        break;
	      }
	      reportConnection();
	    }
	  }
	  catch(Exception e)
	  {
	    mLog.exception(e);
	    mConnectionStatus = false;
	  }
	  finally
	  {
      mcClientConnected.release();//release mutex
	  }
	}

	void reportConnection() {
		try {
			mConnectionStatus = mClientSocket.isConnected();
		} catch (NullPointerException e) {
			mConnectionStatus = false;
			mLog.info("Not connected");
		}
	}

	public void disconnect()
	{
	  mLog.debug("Disconnecting from server");
		try {
			if(mClientSocket != null)
				mClientSocket.close();
		} catch (IOException e) {
			mLog.exception(e);
		}
	}
	
	public EConnectionState connectToClient(String pIpAddress, int pPort)
	{
    EConnectionState returnValue;
    if(!mConnectionStatus)
    {
      mLog.info("Connecting to " + pIpAddress + ":" + pPort);
      returnValue = establishConnectionClient(pIpAddress, pPort);
    } else {
      returnValue = EConnectionState.ESTABLISHED;
    }

    mLog.debug(returnValue);
    reportConnection();
    return returnValue;
	}

	public EConnectionState connect(String pIpAddress, int pPort)
	{
		EConnectionState returnValue;

		mLog.info("Connecting to " + pIpAddress + ":" + pPort);

		if(!mConnectionStatus)
		{
			returnValue = establishConnectionServer(pIpAddress, pPort);
		} else {
			returnValue = EConnectionState.ESTABLISHED;
		}

		mLog.debug(returnValue);
		reportConnection();
		return returnValue;
	}

  public void cancel()
  {
    
  }
	
	EConnectionState establishConnectionClient(String pIpAddress, int pPort)
	{
	  try
    {
	    InetAddress addr = InetAddress.getByName(pIpAddress);
	    mLog.debug("Inet Addr:" + addr);
      mClientSocket = new Socket(addr, pPort);
      mLog.debug("Client Established");
      mcClientConnected = new Semaphore(0,true);//create "mutex" semaphore so user knows when Client is done

      Runtime.getRuntime().addShutdownHook(new Thread(){
        public void run()
        {
          // Clean up the socket
          disconnect();
        }
      });
    } catch (UnknownHostException e)
    {
      mLog.error("Error Connecting to ",pIpAddress,":",pPort, " ", e.getMessage());
//      mLog.exception(e);
      return EConnectionState.ERROR;
    } catch (IOException e) {
      mLog.error("Error Connecting to ",pIpAddress,":",pPort, " ", e.getMessage());
//      mLog.exception(e);
      return EConnectionState.ERROR;
    }
	  return EConnectionState.ESTABLISHED;
	}	

	EConnectionState establishConnectionServer(String pIpAddress, int pPort) {
		try {
			mServerSocket = new ServerSocket(pPort, 2, null);

			mLog.debug("Server Attempt Accept");
			mClientSocket = mServerSocket.accept();
			mLog.debug(mClientSocket);
			mLog.debug("Server Accept");
			mServerSocket.close();

			//mcClientSocket = new Socket(InetAddress.getByName(pIpAddress), pPort);//create socket to connect to server
			mcClientConnected = new Semaphore(0,true);//create "mutex" semaphore so user knows when Client is done
		} catch (UnknownHostException e) {
			mLog.error("Error Connecting to ",pIpAddress,":",pPort, " ", e.getMessage());
			mLog.exception(e);
			return EConnectionState.ERROR;
		} catch (IOException e) {
			mLog.error("Error Connecting to ",pIpAddress,":",pPort, " ", e.getMessage());
			mLog.exception(e);
			return EConnectionState.ERROR;
		}
		mLog.debug("Server Established");
		return EConnectionState.ESTABLISHED;
	}

	public Socket getSocket() {
		return mClientSocket;
	}

  public static void readInput(InputStream reader, int size, byte[] data) throws Exception
  {
    int amountRead = 0;
    int currentReadData = 0;
    while(amountRead < size) {
      currentReadData = reader.read(data, amountRead, size - amountRead);
      if(currentReadData <= 0) {
        throw new IOException();
      } else {
        amountRead += currentReadData;
      }
    }
  }
}