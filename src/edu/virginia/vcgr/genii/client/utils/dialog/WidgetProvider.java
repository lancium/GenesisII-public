package edu.virginia.vcgr.genii.client.utils.dialog;

/**
 * A widget provider is a factory that creates dialog boxes.  It acts as a
 * common base for determining which types of widgets are used (text, 
 * graphical, etc.).
 * 
 * @author mmm2a
 */
public interface WidgetProvider
{
	/* All of the methods in this provider are obvious -- the create the
	 * widges indicated and assign a default title.
	 */
	
	public MenuWidget createMenuDialog(String title);
	public GenericQuestionWidget createGenericQuestionDialog(String title);
	public OKCancelWidget createOKCancelDialog(String title);
	public YesNoWidget createYesNoDialog(String title);
	public PasswordWidget createPasswordDialog(String title);
}