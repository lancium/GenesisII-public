package edu.virginia.vcgr.genii.client.dialog.gui;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import edu.virginia.vcgr.genii.client.dialog.InformationDialog;
import edu.virginia.vcgr.genii.client.dialog.TextContent;

public class GuiInformationDialog extends AbstractGuiDialog implements
		InformationDialog
{
	static final long serialVersionUID = 0L;
	
	private JTextArea _area;
	
	public GuiInformationDialog(String title, TextContent content)
	{
		super(title);
		
		_area.setText(content.toString());
	}
	
	@Override
	protected JComponent createBody(Object []bodyParameters)
	{
		_area = new JTextArea();
		return new JScrollPane(_area);
	}
}