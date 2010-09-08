package edu.virginia.vcgr.genii.ui.rns;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.plugins.files.EditPlugin;

class DefaultDoubleClickListener implements RNSTreeListener
{
	DefaultDoubleClickListener()
	{
	}
	
	@Override
	final public void pathClicked(RNSTree tree, UIContext context,
		RNSFilledInTreeObject fObj)
	{
		// Do nothing
	}

	@Override
	final public void pathDoubleClicked(RNSTree tree,
		UIContext context, RNSFilledInTreeObject fObj)
	{
		TypeInformation typeInfo = fObj.typeInformation();
		if (typeInfo.isByteIO())
		{
			EditPlugin.performEdit(tree, context, fObj.path());
		}
	}
}