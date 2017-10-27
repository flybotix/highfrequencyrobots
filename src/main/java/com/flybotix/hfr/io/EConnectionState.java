package com.flybotix.hfr.io;


import com.flybotix.hfr.util.lang.IClone;

public enum EConnectionState implements IClone<EConnectionState>
{
	ATTEMPTING,
	ESTABLISHED,
	DISCONNECTED,
	ERROR;
	
	public EConnectionState createClone(){ return EConnectionState.values()[ordinal()]; }
	
	public boolean isConnected()
	{
	  if(this == ESTABLISHED) return true;
	  else return false;
	}
		
}
