package com.flybotix.hfr.util.log;

public class LogOutput {
	public final ELevel level;
	public final String text;
	public final String thread;
	public final String method;
	public final long time;
	
	LogOutput(long pTime, ELevel pLevel, String pText, String pThread, String pMethod)
	{
		time = pTime;
		level = pLevel;
		text = pText;
		thread = pThread;
		method = pMethod;
	}
	
	public String toString() {
	  return "[" + level + "] [" + thread + "] [" + method + ":\t" + text;
	}
}
