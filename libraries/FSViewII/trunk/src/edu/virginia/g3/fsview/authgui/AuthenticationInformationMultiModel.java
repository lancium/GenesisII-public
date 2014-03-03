package edu.virginia.g3.fsview.authgui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;

import edu.virginia.g3.fsview.FSViewAuthenticationInformation;
import edu.virginia.g3.fsview.FSViewAuthenticationInformationTypes;
import edu.virginia.g3.fsview.gui.AbstractFSViewInformationModel;
import edu.virginia.g3.fsview.gui.AcceptabilityState;
import edu.virginia.g3.fsview.gui.FSViewInformationListener;
import edu.virginia.g3.fsview.gui.FSViewInformationModel;

final public class AuthenticationInformationMultiModel extends AbstractFSViewInformationModel<FSViewAuthenticationInformation>
{
	private Map<FSViewAuthenticationInformationTypes, AuthenticationInformationModel> _models =
		new EnumMap<FSViewAuthenticationInformationTypes, AuthenticationInformationModel>(
			FSViewAuthenticationInformationTypes.class);
	private FSViewAuthenticationInformationTypes _selectedType;
	private boolean _isSingletonAnonymous;

	public AuthenticationInformationMultiModel(FSViewAuthenticationInformationTypes[] supportedTypes)
	{
		super("Multi");

		if (supportedTypes == null || supportedTypes.length == 0)
			supportedTypes = new FSViewAuthenticationInformationTypes[] { FSViewAuthenticationInformationTypes.Anonymous };

		_isSingletonAnonymous =
			(supportedTypes.length == 1) && (supportedTypes[0] == FSViewAuthenticationInformationTypes.Anonymous);

		FSViewInformationListenerImpl listener = new FSViewInformationListenerImpl();

		for (FSViewAuthenticationInformationTypes type : supportedTypes) {
			AuthenticationInformationModel model = (AuthenticationInformationModel) type.createModel();

			_models.put(type, model);
			model.addInformationListener(listener);
		}

		Arrays.sort(supportedTypes);
		_selectedType = supportedTypes[0];
	}

	final void select(FSViewAuthenticationInformationTypes newCurrent)
	{
		_selectedType = newCurrent;
		fireContentsChanged();
	}

	final Set<FSViewAuthenticationInformationTypes> authenticationTypes()
	{
		return _models.keySet();
	}

	final AuthenticationInformationModel model(FSViewAuthenticationInformationTypes type)
	{
		return _models.get(type);
	}

	@Override
	final public AcceptabilityState isAcceptable()
	{
		return _models.get(_selectedType).isAcceptable();
	}

	@Override
	final public FSViewAuthenticationInformation wrap()
	{
		return _models.get(_selectedType).wrap();
	}

	@Override
	final public Component createGuiComponent()
	{
		JPanel panel = new JPanel(new GridBagLayout());

		if (!_isSingletonAnonymous) {
			panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				"Authentication Information"));

			if (_models.size() == 1) {
				panel.add(_models.get(_selectedType).createGuiComponent(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
			} else {
				ButtonGroup group = new ButtonGroup();
				FSViewAuthenticationInformationTypes[] types =
					_models.keySet().toArray(new FSViewAuthenticationInformationTypes[_models.size()]);
				Arrays.sort(types);
				CardLayout cardLayout = new CardLayout();
				JPanel cardPanel = new JPanel(cardLayout);
				cardPanel.setBorder(BorderFactory.createLoweredBevelBorder());

				for (int lcv = 0; lcv < types.length; lcv++) {
					JRadioButton button = new JRadioButton(new RadioAction(cardPanel, cardLayout, types[lcv]));
					group.add(button);

					panel.add(button, new GridBagConstraints(0, lcv, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

					cardPanel.add(_models.get(types[lcv]).createGuiComponent(), types[lcv].name());
				}

				cardLayout.show(cardPanel, _selectedType.name());
				panel.add(cardPanel, new GridBagConstraints(1, 0, 1, types.length, 1.0, 1.0, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
			}
		}

		return panel;
	}

	private class RadioAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private JPanel _panel;
		private CardLayout _layout;
		private FSViewAuthenticationInformationTypes _buttonType;

		private RadioAction(JPanel panel, CardLayout layout, FSViewAuthenticationInformationTypes buttonType)
		{
			super(buttonType.toString());

			putValue(Action.SELECTED_KEY, buttonType == _selectedType);
			_buttonType = buttonType;

			_panel = panel;
			_layout = layout;
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			_layout.show(_panel, _buttonType.name());
			select(_buttonType);
		}
	}

	private class FSViewInformationListenerImpl implements FSViewInformationListener<FSViewAuthenticationInformation>
	{
		@Override
		public void contentsChanged(FSViewInformationModel<FSViewAuthenticationInformation> model)
		{
			fireContentsChanged();
		}
	}
}