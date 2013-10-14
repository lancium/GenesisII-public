package edu.virginia.vcgr.genii.ui.utils;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

public class SimplePanel extends JPanel
{
	static final long serialVersionUID = 0L;

	private SimplePanel(boolean isHorizontal, int gridBagAnchor, int gridBagFill, Component... components)
	{
		super(new GridBagLayout());

		for (int lcv = 0; lcv < components.length; lcv++) {
			add(components[lcv], new GridBagConstraints(isHorizontal ? lcv : 0, isHorizontal ? 0 : lcv, 1, 1, 1.0, 1.0,
				gridBagAnchor, gridBagFill, new Insets(5, 5, 5, 5), 5, 5));
		}
	}

	static public JPanel createHorizontalPanel(int gridBagAnchor, int gridBagFill, Component... components)
	{
		return new SimplePanel(true, gridBagAnchor, gridBagFill, components);
	}

	static public JPanel createVerticalPanel(int gridBagAnchor, int gridBagFill, Component... components)
	{
		return new SimplePanel(false, gridBagAnchor, gridBagFill, components);
	}
}