package edu.virginia.g3.fsview.gui;

public interface FSViewInformationListener<InfoType> {
	public void contentsChanged(FSViewInformationModel<InfoType> model);
}