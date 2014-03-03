package edu.virginia.vcgr.genii.gjt.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.AbstractAction;
import javax.swing.JLabel;

import edu.virginia.vcgr.genii.gjt.data.JobDocument;
import edu.virginia.vcgr.genii.gjt.data.ModificationListener;
import edu.virginia.vcgr.genii.gjt.data.analyze.Analysis;
import edu.virginia.vcgr.genii.gjt.gui.util.TitledPanel;

class ErrorPanel extends TitledPanel implements ModificationListener {
	static final long serialVersionUID = 0L;

	private JobDocument _document;
	private AbstractAction _generateAction;
	private JLabel _label = new JLabel();

	private void analyze() {
		Analysis analysis = _document.analyze();

		if (analysis.hasErrors()) {
			_generateAction.setEnabled(false);
			_label.setForeground(Color.red);
			_label.setText(analysis.errors().iterator().next());

		} else if (analysis.hasWarnings()) {
			_generateAction.setEnabled(true);
			_label.setForeground(Color.black);
			_label.setText(analysis.warnings().iterator().next());
		} else {
			_generateAction.setEnabled(true);
			_label.setText(" ");
		}
	}

	ErrorPanel(AbstractAction generateAction, JobDocument document) {
		super("Warnings & Errors", new GridBagLayout());

		_generateAction = generateAction;
		_document = document;

		add(_label, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
						5, 5, 5), 5, 5));

		analyze();
	}

	@Override
	public void jobDescriptionModified() {
		analyze();
	}
}