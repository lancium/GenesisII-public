package edu.virginia.vcgr.genii.gjt.gui.resource;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JLabel;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;
import edu.virginia.vcgr.genii.gjt.data.JobDocument;
import edu.virginia.vcgr.genii.gjt.gui.util.TitledPanel;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

class ProcessorArchitecturePanel extends TitledPanel {
	static final long serialVersionUID = 0L;

	ProcessorArchitecturePanel(JobDocumentContext context) {
		super("Processor Architecture", new GridBagLayout());

		ProcessorArchitectureComboBox arch = new ProcessorArchitectureComboBox(
				context.applicationContext().preferences());

		arch.setSelectedItem(context.jobDocument().processorArchitecture());
		arch.addItemListener(new ArchListener(context.jobDocument()));

		add(new JLabel("Architecture"), new GridBagConstraints(0, 0, 1, 1, 0.0,
				1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		add(arch, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
	}

	private class ArchListener implements ItemListener {
		private JobDocument _doc;

		private ArchListener(JobDocument doc) {
			_doc = doc;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED)
				_doc.processorArchitecture((ProcessorArchitecture) e.getItem());
			else
				_doc.processorArchitecture(null);
		}
	}
}