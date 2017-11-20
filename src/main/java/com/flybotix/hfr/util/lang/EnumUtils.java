package com.flybotix.hfr.util.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class EnumUtils {
  
  public static <E extends Enum<E>> List<E> getSortedEnums(Class<E> pEnumeration) {
    return getEnums(pEnumeration, true);
  }
  
  public static <E extends Enum<E>> List<E> getEnums(Class<E> pEnumeration) {
    return getEnums(pEnumeration, false);
  }
  
  public static <E extends Enum<E>> List<E> getEnums(Class<E> pEnumeration, boolean pSorted) {
    Set<E> set = EnumSet.allOf(pEnumeration);
    List<E> result = new ArrayList<E>();
    result.addAll(set);
    Collections.sort(result, (e1, e2) -> Integer.compare(e1.ordinal(), e2.ordinal()));
    return result;
  }
  
  public static <E extends Enum<E>> int getLength(Class<E> pEnumeration) {
    return getEnums(pEnumeration, false).size();
  }
  
  @SuppressWarnings("unchecked")
  public static <E extends Enum<E>> Class<E> getEnumClass(String pString) {
    try {
      return (Class<E>)EnumUtils.class.getClassLoader().loadClass(pString);
    } catch (ClassNotFoundException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
    return null;
  }
  
  public static <E extends Enum<E>> E getFirstEnumInstance(Class<E> pEnum) {
    return getSortedEnums(pEnum).get(0);
  }
  
  public static <E extends Enum<E>> List<E> classLoadEnums(String pClass) {
    Class<E> clazz = getEnumClass(pClass);
    return getSortedEnums(clazz);
  }
  
  public static <E extends Enum<E>> int hashOf(Class<E> pEnumeration) {
    Set<E> set = EnumSet.allOf(pEnumeration);
    int p = 31;
    int result = 1;
    for(E e : set) {
      result = p * result + e.name().hashCode();
    }
    return result;
  }
}
