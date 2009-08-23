package edu.virginia.vcgr.genii.container.dynpages;

import java.io.IOException;
import java.io.PrintStream;

public abstract class SimpleTitledHtmlPage implements DynamicPage
{
	private String _title;
	
	protected abstract void generateBody(PrintStream ps) throws IOException;
	
	protected SimpleTitledHtmlPage(String title)
	{
		_title = title;
	}
	
	@Override
	final public void generate(PrintStream ps) throws IOException
	{
		ps.format("<HTML><TITLE>%s</TITLE><BODY><CENTER><H1>%s</H1></CENTER><BR>",
			_title, _title);
		generateBody(ps);
		ps.println("</BODY></HTML>");
	}
}
