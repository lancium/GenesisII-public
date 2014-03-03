package edu.virginia.vcgr.genii.gjt.gui.resource;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JLabel;
import javax.swing.JSpinner;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;
import edu.virginia.vcgr.genii.gjt.conf.SPMDVariation;
import edu.virginia.vcgr.genii.gjt.data.JobDocument;
import edu.virginia.vcgr.genii.gjt.gui.util.TitledPanel;

class SPMDPanel extends TitledPanel {
	static final long serialVersionUID = 0L;

	SPMDPanel(JobDocumentContext context) {
		super("Parallel Job Information", new GridBagLayout());

		SPMDVariationComboBox variation = new SPMDVariationComboBox();
		variation.setSelectedItem(context.jobDocument().spmdVariation());
		variation.addItemListener(new SPMDValueListener(context.jobDocument()));

		JSpinner numProces = new NullableNumberSpinner(
				new NullableNumberSpinnerModel(context.jobDocument()
						.numberOfProcesses(), 1, Long.MAX_VALUE, 1));
		JSpinner procesPerHost = new NullableNumberSpinner(
				new NullableNumberSpinnerModel(context.jobDocument()
						.processesPerHost(), 1, Long.MAX_VALUE, 1));

		add(new JLabel("Parallel Environment"), new GridBagConstraints(0, 0, 1,
				1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		add(variation, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Number of Processes"), new GridBagConstraints(2, 0, 1,
				1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		add(numProces, new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Processes per Host"), new GridBagConstraints(4, 0, 1,
				1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		add(procesPerHost, new GridBagConstraints(5, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
	}

	private class SPMDValueListener implements ItemListener {
		private JobDocument _document;

		private SPMDValueListener(JobDocument doc) {
			_document = doc;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED)
				_document.spmdVariation((SPMDVariation) e.getItem());
			else
				_document.spmdVariation(null);
		}
	}
}
