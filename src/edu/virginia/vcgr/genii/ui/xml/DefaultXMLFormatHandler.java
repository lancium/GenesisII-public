package edu.virginia.vcgr.genii.ui.xml;

import java.io.IOException;
import java.io.PrintStream;

public class DefaultXMLFormatHandler implements XMLFormatHandler
{
	private PrintStream _out;

	public DefaultXMLFormatHandler(PrintStream out)
	{
		_out = out;
	}

	@Override
	public void appendText(String text) throws IOException
	{
		_out.print(text);
	}

	@Override
	public void startElement() throws IOException
	{
		// Do nothing
	}

	@Override
	public void startAttribute() throws IOException
	{
		// Do nothing
	}

	@Override
	public void endElement() throws IOException
	{
		// Do nothing
	}

	@Override
	public void endAttribute() throws IOException
	{
		// Do nothing
	}
}