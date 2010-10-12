package edu.virginia.vcgr.genii.ui.plugins.acls;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import edu.virginia.vcgr.genii.ui.ApplicationContext;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.UIFrame;

class ACLTearoffWindow extends UIFrame
{
	static final long serialVersionUID = 0L;
	
	ACLTearoffWindow(ApplicationContext applicationContext,
		UIContext uiContext, ACLPanel newPanel)
	{
		super(uiContext, 
			String.format("ACLs for %s", newPanel.targetPath().pwd()));
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		content.add(newPanel, new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		
		getMenuFactory().addHelpMenu(_uiContext, getJMenuBar());
	}
}