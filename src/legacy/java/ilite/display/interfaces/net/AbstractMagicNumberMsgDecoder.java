package ilite.display.interfaces.net;

import ilite.util.lang.Delegator;

public abstract class AbstractMagicNumberMsgDecoder<T> extends Delegator<T>
{
  private final int mMagicNumber;
  public AbstractMagicNumberMsgDecoder(int pMagicNumber)
  {
    mMagicNumber = pMagicNumber;
  }
  
  public void decode(byte[] pBytes, int pLength)
  {
    T object = decodeImpl(pBytes, pLength);
    update(object);
  }
  
  public int getMagicNumber()
  {
    return mMagicNumber;
  }
  
  protected abstract T decodeImpl(byte[] pBytes, int pLength);
}
