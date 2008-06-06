package edu.virginia.vcgr.genii.client.dialog;

public interface Dialog
{
	public void setHelp(TextContent helpContent);
	public TextContent getHelp();
	
	public void showDialog() throws DialogException, UserCancelException;
}