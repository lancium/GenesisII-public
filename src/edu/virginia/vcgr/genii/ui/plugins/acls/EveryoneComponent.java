package edu.virginia.vcgr.genii.ui.plugins.acls;

import edu.virginia.vcgr.genii.ui.UIContext;

class EveryoneComponent extends DraggableImageComponent
{
	static final long serialVersionUID = 0L;

	public EveryoneComponent(UIContext context)
	{
		super(ACLImages.everyone());
	
		setTransferHandler(new EveryoneTransferHandler(context));
	}
}