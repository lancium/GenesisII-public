package edu.virginia.vcgr.genii.ui.xml;

import java.io.IOException;

public interface XMLFormatHandler
{
	public void startElement() throws IOException;

	public void startAttribute() throws IOException;

	public void endAttribute() throws IOException;

	public void endElement() throws IOException;

	public void appendText(String text) throws IOException;
}