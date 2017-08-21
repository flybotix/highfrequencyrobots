package com.flybotix.hfr.util.log;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.flybotix.hfr.util.lang.IUpdate;

public class LogHistory implements IUpdate<LogOutput>{
	private LinkedList<LogOutput> mHistory = new LinkedList<LogOutput>();
	private int mMaxSize = 50;
	
	@Override public void update(LogOutput pOutput)
	{
		addOutput(pOutput);
	}
	
	/*package*/ LogHistory()
	{
	}
	
	/*package*/ void addOutput(LogOutput pOutput)
	{
		mHistory.addLast(pOutput);
		if(mHistory.size() > mMaxSize)
		{
			mHistory.removeFirst();
		}
	}
	
	List<LogOutput> getHistory()
	{
		return new ArrayList<LogOutput>(mHistory);
	}
}
