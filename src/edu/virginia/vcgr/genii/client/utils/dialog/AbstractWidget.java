package edu.virginia.vcgr.genii.client.utils.dialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.io.FileResource;

/**
 * An abstract implementation of the widget base class that implements
 * common functionallity found in all types of widgets.
 * 
 * @author mmm2a
 */
public abstract class AbstractWidget implements Widget
{
	private WidgetProvider _provider;
	
	private String _detailedHelp;
	private String _title;
	
	/**
	 * Get the detailed help message for this widget.
	 * 
	 * @return The detailed help message.
	 */
	protected String getDetailedHelp()
	{
		return _detailedHelp;
	}
	
	/**
	 * Get the widget provider for this widget.
	 * 
	 * @return The widget provider.
	 */
	protected WidgetProvider getProvider()
	{
		return _provider;
	}
	
	/**
	 * Get the title for this widget.
	 * 
	 * @return The title.
	 */
	protected String getTitle()
	{
		return _title;
	}
	
	/**
	 * Create a new abstract widget.
	 * 
	 * @param provider The provider responsible for this widget.  This value
	 * cannot be null.
	 * @param title The title to give this widget.  This parameter cannot be 
	 * null.
	 */
	protected AbstractWidget(WidgetProvider provider, String title)
	{
		if (provider == null)
			throw new IllegalArgumentException("Provider cannot be null.");
		
		_provider = provider;
		
		setTitle(title);
		setDetailedHelp((String)null);
	}
	
	@Override
	public void setDetailedHelp(String detailedHelp)
	{
		_detailedHelp = detailedHelp;
	}

	@Override
	public void setDetailedHelp(FileResource detailedHelpResource)
		throws IOException
	{
		_detailedHelp = readResource(detailedHelpResource);
	}

	@Override
	public void setTitle(String title)
	{
		if (title == null)
			title = "";
		
		_title = title;
	}

	static private String readResource(FileResource resource)
		throws IOException
	{
		InputStream in = null;
		StringBuilder builder = new StringBuilder();
		String line;
		
		
		if (resource == null)
			return null;
		
		try
		{
			in = resource.open();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while ( (line = reader.readLine()) != null)
			{
				builder.append(line);
				builder.append('\n');
			}
			
			return builder.toString();
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
}