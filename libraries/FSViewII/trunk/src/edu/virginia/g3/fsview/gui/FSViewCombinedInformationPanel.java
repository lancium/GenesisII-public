package edu.virginia.g3.fsview.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

final class FSViewCombinedInformationPanel extends JPanel {
	static final long serialVersionUID = 0L;

	private FSViewCombinedInformationModel _model;

	FSViewCombinedInformationPanel(FSViewCombinedInformationModel model) {
		super(new GridBagLayout());

		_model = model;

		add(model.viewModel().createGuiComponent(), new GridBagConstraints(0,
				0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(model.authModel().createGuiComponent(), new GridBagConstraints(0,
				1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

		setName(model.viewModel().modelName());
	}

	final FSViewCombinedInformationModel model() {
		return _model;
	}
}
