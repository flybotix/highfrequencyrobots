package com.flybotix.hfr.util.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.flybotix.hfr.util.lang.IUpdate;

public class Logger implements ILog {
	public static void registerOutputListener(IUpdate<LogOutput> pListener)
	{
		LoggingControls.INST.addListener(pListener);
	}
	
	public static void deregisterOutputListener(IUpdate<LogOutput> pListener)
	{
		LoggingControls.INST.removeListener(pListener);
	}
	
	private Logger() {}

	@Override
	public void error(Object... pOutputs) {
		LoggingControls.INST.log(ELevel.ERROR, generateString(pOutputs));
	}

	@Override
	public void exception(Exception pException) {
		LoggingControls.INST.logException(pException);
	}

	@Override
	public void debug(Object... pOutputs) {
	  if(isEnabled(ELevel.DEBUG))
	    LoggingControls.INST.log(ELevel.DEBUG, generateString(pOutputs));
	}

	@Override
	public void info(Object... pOutputs) {
    if(isEnabled(ELevel.INFO))
		LoggingControls.INST.log(ELevel.INFO, generateString(pOutputs));

	}

	@Override
	public void warn(Object... pOutputs) {
    if(isEnabled(ELevel.WARN))
		LoggingControls.INST.log(ELevel.WARN, generateString(pOutputs));

	}
	
	public static void setLevel(ELevel pLevel) {
	  LoggingControls.INST.setOutputLevel(pLevel);
	}

	public static ILog createLog(Class<?> pClass) {
		ILog result = sDEBUGS.get(pClass);
		if (result == null) {
			result = new Logger();
			sDEBUGS.put(pClass, result);
		}
		return result;
	}
	
	public static List<LogOutput> getRecentLogs()
	{
		return LoggingControls.INST.getHistory();
	}

	private static String generateString(Object... pOutputs) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pOutputs.length; i++) {
			sb.append(pOutputs[i]);
		}
		return sb.toString();
	}

	private static Map<Class<?>, ILog> sDEBUGS = new HashMap<Class<?>, ILog>();

  public static boolean isEnabled(ELevel debug) {
    return LoggingControls.INST.isLevelEnabled(debug);
  }
}
