package com.flybotix.hfr.util.lang;

import java.util.concurrent.CopyOnWriteArrayList;

import com.flybotix.hfr.util.log.ILog;
import com.flybotix.hfr.util.log.Logger;

/**
 * This class handles some of the boilerplate involved in setting up custom listener interfaces. It
 * allows you to specify an interface; this class manages a list of implementations, and allows you
 * to get an implementation of that interface that will forward each call to all the listeners in
 * this Delegator. THe passed-in class must be an actual interface, and may not have any methods
 * with a return type other than void. This class is thread-safe. The list of listeners is managed
 * in a safe manner (using a CopyOnWriteArrayList). Calls on the object returned by getDelegator
 * will block until all registered listeners have finished processing the message; the calls to the
 * registered listener will occur on that thread.
 *
 * @param <T> The type of object that will be updated
 */

public class Delegator<T> implements IProvider<T> {
  private CopyOnWriteArrayList<IUpdate<T>> mListeners;
  private ILog mLog = Logger.createLog(Delegator.class);
  protected T mLatest = null;

  public Delegator() {
    mListeners = new CopyOnWriteArrayList<>();
  }

  protected final void update(T pUpdate) {
    mLatest = pUpdate;
    for (IUpdate<T> u : mListeners) {
      try {
        u.update(pUpdate);
      } catch (Exception e) {
        mLog.exception(e);
      }
    }
  }

  @Override
  public void addListener(IUpdate<T> pDelegate) {
    mListeners.add(pDelegate);
  }

  @Override
  public void removeListener(IUpdate<T> pDelegate) {
    mListeners.remove(pDelegate);
  }

  public boolean contains(IUpdate<T> pDelegate) {
    return mListeners.contains(pDelegate);
  }

  public void clear() {
    mListeners.clear();
  }

  @Override
  public T getLatest() {
    return mLatest;
  }
}
