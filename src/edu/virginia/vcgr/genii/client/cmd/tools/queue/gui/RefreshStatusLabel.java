package edu.virginia.vcgr.genii.client.cmd.tools.queue.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

public class RefreshStatusLabel extends JComponent 
	implements RefreshListener
{
	static final long serialVersionUID = 0L;

	private class TextUpdater implements Runnable
	{
		private String _text;
		
		private TextUpdater(String text)
		{
			_text = text;
		}
		
		@Override
		public void run()
		{
			setText(_text);
		}
	}
	
	private JLabel _label;
	
	public RefreshStatusLabel(String text)
	{
		setLayout(new GridBagLayout());
		
		_label = new JLabel(text);
		add(_label, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
			"Status"));
	}

	public void setText(String text)
	{
		if (!SwingUtilities.isEventDispatchThread())
			SwingUtilities.invokeLater(new TextUpdater(text));
		else
			_label.setText(text);
	}
	
	@Override
	public void refreshEnded()
	{
		setText(" ");
	}

	@Override
	public void refreshStarted()
	{
		setText("Updating contents");
	}
}