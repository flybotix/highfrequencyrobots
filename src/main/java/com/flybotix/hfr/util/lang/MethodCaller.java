package com.flybotix.hfr.util.lang;

import java.lang.reflect.InvocationTargetException;

public class MethodCaller {

  public static <E extends Enum<E>> Boolean getBooleanFromEnum(Class<E> pClass, String pMethod, Boolean pDefault) {
    E inst = EnumUtils.getFirstEnumInstance(pClass);
    Boolean result = null;
    try {
      result = Boolean.valueOf(pClass.getMethod(pMethod).invoke(inst).toString());
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
      | NoSuchMethodException | SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return NullSafe.replaceNull(result, pDefault);
  }
}
