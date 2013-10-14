package edu.virginia.g3.fsview.authgui.anon;

import java.awt.Component;

import javax.swing.JPanel;

import edu.virginia.g3.fsview.AnonymousAuthenticationInformation;
import edu.virginia.g3.fsview.FSViewAuthenticationInformation;
import edu.virginia.g3.fsview.FSViewAuthenticationInformationTypes;
import edu.virginia.g3.fsview.authgui.AbstractAuthenticationInformationModel;
import edu.virginia.g3.fsview.gui.AcceptabilityState;

final class AnonymousAuthenticationInformationModel extends AbstractAuthenticationInformationModel
{
	public AnonymousAuthenticationInformationModel()
	{
		super(FSViewAuthenticationInformationTypes.Anonymous);
	}

	@Override
	final public AcceptabilityState isAcceptable()
	{
		return AcceptabilityState.accept(AnonymousAuthenticationInformationModel.class);
	}

	@Override
	final public FSViewAuthenticationInformation wrap()
	{
		return new AnonymousAuthenticationInformation();
	}

	@Override
	final public Component createGuiComponent()
	{
		return new JPanel();
	}
}