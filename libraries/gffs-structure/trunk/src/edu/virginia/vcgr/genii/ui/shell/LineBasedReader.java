package edu.virginia.vcgr.genii.ui.shell;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.util.LinkedList;

public class LineBasedReader extends Reader
{
	private boolean _closed = false;
	private LinkedList<String> _lines = new LinkedList<String>();

	@Override
	public void close() throws IOException
	{
		synchronized (_lines) {
			_closed = true;
			_lines.notifyAll();
		}
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException
	{
		int ret = 0;

		try {
			synchronized (_lines) {
				while (true) {
					if (_closed)
						return -1;

					if (_lines.isEmpty())
						_lines.wait();
					else {
						while (!_lines.isEmpty()) {
							String line = _lines.removeFirst();
							int toCopy = Math.min(len, line.length());
							line.getChars(0, toCopy, cbuf, off);
							len -= toCopy;
							ret += toCopy;
							off += toCopy;
							if (toCopy < line.length())
								_lines.addFirst(line.substring(toCopy));
							if (len <= 0)
								return ret;
						}

						return ret;
					}
				}
			}
		} catch (InterruptedException ie) {
			throw new InterruptedIOException();
		}
	}

	public void addLine(String line)
	{
		synchronized (_lines) {
			_lines.addLast(line + "\n");
			_lines.notifyAll();
		}
	}
}