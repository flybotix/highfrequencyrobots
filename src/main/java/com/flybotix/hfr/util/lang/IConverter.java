package com.flybotix.hfr.util.lang;

public interface IConverter <FROM,TO>{
  public TO convert(FROM pElement);
}
