package edu.virginia.vcgr.genii.client.dialog.text;

import edu.virginia.vcgr.genii.client.dialog.Dialog;
import edu.virginia.vcgr.genii.client.dialog.TextContent;

abstract class AbstractTextDialog implements Dialog
{
	private TextContent _help;
	private String _title;
	
	protected ConsolePackage _package;
	
	protected AbstractTextDialog(String title, ConsolePackage pkg)
	{
		_help = null;
		_title = title;
		_package = pkg;
		
		if (_title != null)
		{
			_package.stdout().print("**");
			for (int lcv = 0; lcv < _title.length(); lcv++)
				_package.stdout().print("*");
			_package.stdout().println("**");
			
			_package.stdout().print("* ");
			_package.stdout().print(_title);
			_package.stdout().println(" *");
			
			_package.stdout().print("**");
			for (int lcv = 0; lcv < _title.length(); lcv++)
				_package.stdout().print("*");
			_package.stdout().println("**");
		} else
		{
			_package.stdout().println("*********************************");
		}
		
		_package.stdout().println();
	}
	
	@Override
	public TextContent getHelp()
	{
		return _help;
	}

	@Override
	public void setHelp(TextContent helpContent)
	{
		_help = helpContent;
	}
}