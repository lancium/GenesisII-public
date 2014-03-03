package edu.virginia.g3.fsview.file;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.net.URI;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import edu.virginia.g3.fsview.gui.AbstractFSViewInformationModel;
import edu.virginia.g3.fsview.gui.AcceptabilityState;

final class FileFSViewInformationModel extends
		AbstractFSViewInformationModel<URI> {
	private String _filesystemPath;

	FileFSViewInformationModel() {
		super("Filesystem");

		filesystemPath("");
	}

	void filesystemPath(String filesystemPath) {
		if (filesystemPath == null)
			filesystemPath = "";

		_filesystemPath = filesystemPath;

		fireContentsChanged();
	}

	@Override
	final public AcceptabilityState isAcceptable() {
		if (_filesystemPath.length() > 0)
			return AcceptabilityState.accept(FileFSViewInformationModel.class);

		return AcceptabilityState.deny(FileFSViewInformationModel.class,
				"Filesystem path cannot be empty");
	}

	@Override
	final public URI wrap() {
		File file = new File(_filesystemPath);
		return file.toURI();
	}

	@Override
	final public Component createGuiComponent() {
		JPanel panel = new JPanel(new GridBagLayout());

		panel.add(new JLabel("Filesystem Path"), new GridBagConstraints(0, 0,
				1, 1, 0.0, 1.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		JTextField field = new JTextField(32);
		panel.add(field, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		field.addCaretListener(new CaretListener() {
			@Override
			final public void caretUpdate(CaretEvent e) {
				JTextField field = (JTextField) e.getSource();
				filesystemPath(field.getText());
			}
		});

		return panel;
	}
}