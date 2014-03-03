package edu.virginia.g3.fsview.http;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URI;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import edu.virginia.g3.fsview.gui.AbstractFSViewInformationModel;
import edu.virginia.g3.fsview.gui.AcceptabilityState;

final class HTTPFSViewInformationModel extends
		AbstractFSViewInformationModel<URI> {
	private String _url;

	HTTPFSViewInformationModel() {
		super("HTTP(S) Filesystem");

		url("");
	}

	void url(String url) {
		if (url == null)
			url = "";

		_url = url;

		fireContentsChanged();
	}

	@Override
	final public AcceptabilityState isAcceptable() {
		if (_url.length() > 0)
			return AcceptabilityState.accept(HTTPFSViewInformationModel.class);

		return AcceptabilityState.deny(HTTPFSViewInformationModel.class,
				"URL cannot be empty");
	}

	@Override
	final public URI wrap() {
		return URI.create(_url);
	}

	@Override
	final public Component createGuiComponent() {
		JPanel panel = new JPanel(new GridBagLayout());

		panel.add(new JLabel("HTTP(S) URL"), new GridBagConstraints(0, 0, 1, 1,
				0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		JTextField field = new JTextField(32);
		panel.add(field, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		field.addCaretListener(new CaretListener() {
			@Override
			final public void caretUpdate(CaretEvent e) {
				JTextField field = (JTextField) e.getSource();
				url(field.getText());
			}
		});

		return panel;
	}
}