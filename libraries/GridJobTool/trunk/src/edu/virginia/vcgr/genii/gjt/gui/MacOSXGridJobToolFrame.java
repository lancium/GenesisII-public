package edu.virginia.vcgr.genii.gjt.gui;

import javax.swing.Action;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;

class MacOSXGridJobToolFrame extends GridJobToolFrame
{
	static final long serialVersionUID = 0L;

	@Override
	protected Action getExitAction()
	{
		return null;
	}

	@Override
	protected Action getPreferencesAction()
	{
		return null;
	}

	@Override
	protected Action getAboutAction()
	{
		return null;
	}

	MacOSXGridJobToolFrame(JobDocumentContext documentContext)
	{
		super(documentContext);
	}
}