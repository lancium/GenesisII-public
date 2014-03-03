package edu.virginia.vcgr.genii.gjt.gui.resource;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JLabel;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;
import edu.virginia.vcgr.genii.gjt.data.JobDocument;
import edu.virginia.vcgr.genii.gjt.gui.util.ParameterizableStringField;
import edu.virginia.vcgr.genii.gjt.gui.util.TitledPanel;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;

class OperatingSystemPanel extends TitledPanel {
	static final long serialVersionUID = 0L;

	OperatingSystemPanel(JobDocumentContext context) {
		super("Operating System", new GridBagLayout());

		OperatingSystemComboBox osCombo = new OperatingSystemComboBox(context
				.applicationContext().preferences());
		ParameterizableStringField osVersion = new ParameterizableStringField(
				context.jobDocument().operatingSystemVersion(), 8);

		osCombo.setSelectedItem(context.jobDocument().operatingSystem());
		osCombo.addItemListener(new OSSelectionListener(context.jobDocument()));

		add(new JLabel("Type"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
						5, 5, 5), 5, 5));
		add(osCombo, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Version"), new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		add(osVersion, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
	}

	private class OSSelectionListener implements ItemListener {
		private JobDocument _doc;

		private OSSelectionListener(JobDocument doc) {
			_doc = doc;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED)
				_doc.operatingSystem((OperatingSystemNames) e.getItem());
			else
				_doc.operatingSystem(null);
		}
	}
}