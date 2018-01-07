package com.flybotix.hfr.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.flybotix.hfr.codex.CodexOf;

public class CodexElementHistory <V, E extends Enum<E> & CodexOf<V>> {

  private final ArrayList<CodexElementInstance<V, E>> mList;
  private final E mEnum;
  private final int mMaxPoints;
  
  public CodexElementHistory(E pEnum, int pNumPointsToKeep) {
    mList = new ArrayList<>(pNumPointsToKeep);
    mEnum = pEnum;
    mMaxPoints = pNumPointsToKeep;
  }
  
  public synchronized void add(double pTime, V pDataPoint) {
    mList.add(new CodexElementInstance<>(pTime, pDataPoint, mEnum));
    if(mList.size() > mMaxPoints) {
      mList.remove(0);
    }
  }
  
  public synchronized void clear() {
	  mList.clear();
  }
  
  public List<CodexElementInstance<V,E>> getData() {
    return new ArrayList<>(mList);
  }
  
  public Stream<CodexElementInstance<V,E>> stream() {
    return mList.stream();
  }
}
