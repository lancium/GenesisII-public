package org.morgan.util.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

public class TitledPanel extends JPanel
{
	static final long serialVersionUID = 0L;
	
	public TitledPanel(String title, Component contents)
	{
		this(title);
		
		add(contents, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(5, 5, 5, 5), 5, 5));
	}
	
	public TitledPanel(String title)
	{
		super(new GridBagLayout());

		setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
			title));
	}
}