package com.flybotix.hfr.io;

import java.awt.Color;

import com.flybotix.hfr.util.lang.IClone;

public enum EConnectionState implements IClone<EConnectionState>
{
	ATTEMPTING(Color.BLUE),
	ESTABLISHED(Color.GREEN),
	DISCONNECTED(Color.gray),
	ERROR(Color.red);
	
	public EConnectionState createClone(){ return EConnectionState.values()[ordinal()]; }
	
	public boolean isConnected()
	{
	  if(this == ESTABLISHED) return true;
	  else return false;
	}
		
	public Color color() { return mColor; }
	
	private Color mColor;
	
	private EConnectionState(Color pColor)
	{
		mColor = pColor;
	}
}
