package edu.virginia.vcgr.genii.gjt.gui.util;

import javax.swing.JComponent;

public interface DescribedFieldEditor<Type>
{
	public void edit(JComponent parentComponent, Type currentValue);
}