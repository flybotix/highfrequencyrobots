package com.flybotix.hfr.util.log;

import java.util.List;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import com.flybotix.hfr.util.lang.Delegator;

class LoggingControls extends Delegator<LogOutput>{
  static final LoggingControls INST = new LoggingControls();

	private int mStackTraceLength = 15;
	private static final Pattern sDEBUG_PACKAGE = Pattern
			.compile(LoggingControls.class.getPackage().getName());
	private static final Pattern sTHREAD_PACKAGE = Pattern.compile(Thread.class
			.getName());

	private LogHistory mHistory = new LogHistory();

  private ELevel mLevel = ELevel.WARN;
	
	List<LogOutput> getHistory()
	{
		return mHistory.getHistory();
	}
	
	private LoggingControls() {
		addListener(mHistory);
		addListener(pUpdate -> {
			if (pUpdate.level == ELevel.ERROR) {
			  System.err.println(pUpdate);
			} else {
				System.out.println(pUpdate);
			}
		});
	}
	
	boolean isLevelEnabled(ELevel pLevel) {
	  return pLevel.ordinal() >= mLevel.ordinal();
	}

	void log(ELevel pLevel, String pText) {
	  if(isLevelEnabled(pLevel)) {
  		LogOutput output = new LogOutput(
  				System.currentTimeMillis(), 
  				pLevel, 
  				pText, 
  				Thread.currentThread().getName(), 
  				getCurrentThreadClassName());
  		update(output);
	  }
	}
	
	void logException(Exception pException)
	{
	  try {
	    log(ELevel.ERROR, format(pException));
	  } catch (Throwable t) {
	    t.printStackTrace();
	  }
	}

	void printStackTrace(ELevel pLevel, String... pNotes) {
		printStackTrace(pLevel, -1L, pNotes);
	}
	
	void printStackTrace(ELevel pLevel, StackTraceElement[] pElements)
	{
		printStackTrace(pLevel, -1L, pElements, new String[0]);
	}
	
	void setOutputLevel(ELevel pLevel) {
	  mLevel  = pLevel;
	}

	void printStackTrace(ELevel pLevel, long delay,
			StackTraceElement[] pElements, String... pNotes) {
		StringBuilder sb = new StringBuilder();
		// sb.append("\n******************** ").append(
		// getClassName(elements[index].getClassName())).append("::").append(
		// elements[index].getMethodName()).append("() *************************\n");
		if (SwingUtilities.isEventDispatchThread()) {
			sb.append("EDT EDT EDT EDT EDT EDT EDT EDT EDT EDT EDT EDT EDT EDT EDT EDT EDT EDT EDT EDT EDT EDT EDT EDT \n");
		}
		if (pNotes != null && pNotes.length > 0) {
			for (String n : pNotes) {
				sb.append("    -  ").append(n).append("\n");
			}
		}
		for (int i = 0; i < pElements.length; i++) {
			if (!sDEBUG_PACKAGE.matcher(pElements[i].getClass().getName())
					.find()) {
				sb.append("   ").append(pElements[i]).append('\n');
			}
			// if(!elements[i].getClass().getName().contains(sDEBUG_PACKAGE)){
			// sb.append("   ").append(elements[i]).append('\n');
			// }
		}
		if (delay >= 0) {
			sb.append("\nCall took ").append(delay).append("ms!\n");
		}
		sb.append("******************************************************\n");
		log(pLevel, sb.toString());

	}

	void printStackTrace(ELevel pLevel, long delay, String... pNotes) {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		printStackTrace(pLevel, delay, elements, pNotes);
	}

	void setStackTraceLength(int pLength) {
		mStackTraceLength = pLength;
	}

	int getStackTraceLength() {
		return mStackTraceLength;
	}

	private String format(Exception pException) {
		StringBuilder sb = new StringBuilder();
		StackTraceElement[] traces = pException.getStackTrace();
		int end = traces.length;

		sb.append(pException.toString()).append("\n");
		for (int i = 0; i < end; i++) {
			sb.append("\t\tat ").append(traces[i]).append("\n");
		}
		return sb.toString();
	}

	private static String getCurrentThreadClassName() {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();

		StackTraceElement element = null;
		for (int i = 0; i < elements.length; i++) {
			element = elements[i];
			boolean foundThread = sTHREAD_PACKAGE.matcher(element.toString())
					.find();
			foundThread |= sDEBUG_PACKAGE.matcher(element.toString()).find();
			if (foundThread == false) {
				break;
			}
		}
		// String rc = "NULL";
		// if(element != null){
		// rc += "["+element.getClassName()+":"+element.getLineNumber();
		// }
		// return rc;
		if (element == null) {
			return "(Unknown class)";
		} else {
			return new StringBuilder(element.getClassName())
					// .append(":")
					// .append(element.getLineNumber())
					.append("::").append(element.getMethodName()).append("()")
					.append(":").append(element.getLineNumber())
					.toString();
		}
	}

}
