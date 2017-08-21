package ilite.display.interfaces.net;

import java.io.InputStream;

public interface IMessageParser {
	public void parse(InputStream is) throws Exception;
}
