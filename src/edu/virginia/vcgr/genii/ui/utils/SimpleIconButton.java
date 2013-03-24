package edu.virginia.vcgr.genii.ui.utils;

import javax.swing.Action;
import javax.swing.JButton;

public class SimpleIconButton extends JButton
{
	static final long serialVersionUID = 0L;

	private void initialize(ShapeIcons icons)
	{
		setRolloverEnabled(true);

		setIcon(icons.normalIcon());
		setRolloverIcon(icons.rolloverIcon());
		setDisabledIcon(icons.disabledIcon());

		setBorderPainted(false);
	}

	public SimpleIconButton(ShapeIcons icons)
	{
		super();

		initialize(icons);
	}

	public SimpleIconButton(ShapeIcons icons, Action action)
	{
		super(action);

		initialize(icons);
	}
}