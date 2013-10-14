package edu.virginia.g3.fsview.gui;

import java.awt.Component;
import java.net.URI;

import edu.virginia.g3.fsview.FSViewAuthenticationInformation;
import edu.virginia.g3.fsview.FSViewConnectionInformation;
import edu.virginia.g3.fsview.FSViewFactory;
import edu.virginia.g3.fsview.authgui.AuthenticationInformationMultiModel;

class FSViewCombinedInformationModel extends AbstractFSViewInformationModel<FSViewConnectionInformation>
{
	private FSViewInformationModel<URI> _viewModel;
	private AuthenticationInformationMultiModel _authModel;

	private FSViewCombinedInformationModel(FSViewInformationModel<URI> factoryModel,
		AuthenticationInformationMultiModel authModel)
	{
		super(factoryModel.modelName());

		_viewModel = factoryModel;
		_authModel = authModel;

		_viewModel.addInformationListener(new FSViewInformationListenerImpl<URI>());
		_authModel.addInformationListener(new FSViewInformationListenerImpl<FSViewAuthenticationInformation>());
	}

	FSViewCombinedInformationModel(FSViewFactory factory)
	{
		this(factory.createModel(), new AuthenticationInformationMultiModel(factory.supportedAuthenticationTypes()));
	}

	FSViewInformationModel<URI> viewModel()
	{
		return _viewModel;
	}

	AuthenticationInformationMultiModel authModel()
	{
		return _authModel;
	}

	@Override
	final public AcceptabilityState isAcceptable()
	{
		AcceptabilityState ret = _viewModel.isAcceptable();
		if (ret.isAcceptable())
			ret = _authModel.isAcceptable();

		return ret;
	}

	@Override
	final public FSViewConnectionInformation wrap()
	{
		return new FSViewConnectionInformation(_viewModel.wrap(), _authModel.wrap());
	}

	@Override
	final public Component createGuiComponent()
	{
		return new FSViewCombinedInformationPanel(this);
	}

	private class FSViewInformationListenerImpl<InfoType> implements FSViewInformationListener<InfoType>
	{
		@Override
		public void contentsChanged(FSViewInformationModel<InfoType> model)
		{
			fireContentsChanged();
		}
	}
}