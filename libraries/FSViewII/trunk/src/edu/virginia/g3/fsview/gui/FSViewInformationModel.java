package edu.virginia.g3.fsview.gui;

import java.awt.Component;

public interface FSViewInformationModel<InfoType>
{
	public void addInformationListener(FSViewInformationListener<InfoType> listener);

	public void removeInformationListener(FSViewInformationListener<InfoType> listener);

	public String modelName();

	public AcceptabilityState isAcceptable();

	public InfoType wrap();

	public Component createGuiComponent();
}