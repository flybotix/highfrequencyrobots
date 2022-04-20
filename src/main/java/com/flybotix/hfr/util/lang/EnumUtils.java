package com.flybotix.hfr.util.lang;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class EnumUtils {
  
  public static <E extends Enum<E>> List<E> getSortedEnums(Class<E> pEnumeration) {
    return getEnums(pEnumeration, true);
  }
  
  public static <E extends Enum<E>> List<E> getEnums(Class<E> pEnumeration) {
    return getEnums(pEnumeration, false);
  }
  
  public static <E extends Enum<E>> List<E> getEnums(Class<E> pEnumeration, boolean pSorted) {
//    Set<E> set = EnumSet.allOf(pEnumeration);
//    List<E> result = new ArrayList<E>();
//    result.addAll(set);
//    Collections.sort(result, (e1, e2) -> Integer.compare(e1.ordinal(), e2.ordinal()));
//    return result;
    return Arrays.asList(pEnumeration.getEnumConstants());
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
  
  private static final Map<Class<?>, Integer> hashes = new HashMap<>();
  
  public synchronized static <E extends Enum<E>> int hashOf(Class<E> pEnumeration) {
    if(hashes.containsKey(pEnumeration)) {
      return hashes.get(pEnumeration);
    } else {
      Set<E> set = EnumSet.allOf(pEnumeration);
      final int p = 31;
      int result = 1;
      for(E e : set) {
        result += p * result + e.name().hashCode();
      }
      hashes.put(pEnumeration, result);
      return result;
    }
  }
  
  @SuppressWarnings("unchecked")
  public synchronized static <E extends Enum<E>> int hashOf(E pEnumeration) {
    return hashOf(pEnumeration.getClass());
  }

  /**
   * Given an enumeration and an annotation class, this method will return all enums that match the target annotation
   * class, mapped to the annotation's metadata. Be sure to set the Annotation RetentionPolicy to RUNTIME and the
   * Target to FIELD
   * .
   * @param enumClass
   * @param annotationType
   * @param <E>
   * @param <A>
   * @return
   */
  public static <E extends Enum<E>, A extends Annotation> Map<E, A> getEnumsAnnotatedWith(
          Class<E> enumClass,
          Class<A> annotationType) {
   // Phew https://www.logicbig.com/how-to/reflection/annotations-on-enum-constants.html
    Map<E, A> map = new LinkedHashMap<>();
    if (enumClass == null || annotationType == null) {
      return map;
    }
    for (E enumConstant : enumClass.getEnumConstants()) {
      Field declaredField = null;
      try {
        declaredField = enumClass.getDeclaredField(enumConstant.name());
      } catch (NoSuchFieldException e) {
        //this exception will never be thrown
        e.printStackTrace();
      }
      if (declaredField != null) {//should never be null
        A annotation = declaredField.getAnnotation(annotationType);
        if (annotation != null) {
          map.put(enumConstant, annotation);
        }
      }
    }
    return map;
  }
}
