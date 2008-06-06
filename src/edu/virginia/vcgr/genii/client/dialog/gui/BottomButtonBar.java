package edu.virginia.vcgr.genii.client.dialog.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;

public class BottomButtonBar extends JPanel
{
	static final long serialVersionUID = 0L;
	
	public BottomButtonBar(JButton...buttons)
	{
		super(new GridBagLayout());
		
		for (int x = 0; x < buttons.length; x++)
		{
			add(buttons[x],
				new GridBagConstraints(x, 0, 1, 1, 1.0, 1.0, 
					GridBagConstraints.CENTER, GridBagConstraints.NONE,
					new Insets(5, 5, 5, 5), 5, 5));
		}
	}
	
	public BottomButtonBar(Action...actions)
	{
		super(new GridBagLayout());
		
		for (int x = 0; x < actions.length; x++)
		{
			add(new JButton(actions[x]),
				new GridBagConstraints(x, 0, 1, 1, 1.0, 1.0, 
					GridBagConstraints.CENTER, GridBagConstraints.NONE,
					new Insets(5, 5, 5, 5), 5, 5));
		}
	}
}