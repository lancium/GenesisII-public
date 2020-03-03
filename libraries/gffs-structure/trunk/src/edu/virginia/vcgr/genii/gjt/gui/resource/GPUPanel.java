package edu.virginia.vcgr.genii.gjt.gui.resource;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JLabel;
import javax.swing.JSpinner;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;
import edu.virginia.vcgr.genii.gjt.data.JobDocument;
import edu.virginia.vcgr.genii.gjt.data.SizeValue;
import edu.virginia.vcgr.genii.gjt.gui.util.TitledPanel;
import edu.virginia.vcgr.genii.gjt.units.SizeUnit;
import edu.virginia.vcgr.jsdl.GPUArchitecture;
import edu.virginia.vcgr.jsdl.GPUProcessorArchitecture;

class GPUPanel extends TitledPanel
{
	static final long serialVersionUID = 0L;

	GPUPanel(JobDocumentContext context, int index)
	{
		super("GPU Architecture", new GridBagLayout());

		GPUComboBox garch = new GPUComboBox(context.applicationContext().preferences());

		garch.setSelectedItem(context.jobRoot().jobDocument().get(index).gpuArchitecture());
		garch.addItemListener(new gpuArchListener(context.jobRoot().jobDocument().get(index)));
		
		JSpinner gpuCountPerNode = new NullableNumberSpinner(
			new NullableNumberSpinnerModel(context.jobRoot().jobDocument().get(index).gpuCountPerNode(), 1, Long.MAX_VALUE, 1));
		
		SizeValue gpuCurrentMem = context.jobRoot().jobDocument().get(index).gpuMemoryUpperBound();
		JSpinner upperBound = new UnitValueSpinner(new UnitValueSpinnerModel<SizeUnit>(gpuCurrentMem, 1, Long.MAX_VALUE, 1));
		SizeUnitValueComboBox upperBoundUnit = new SizeUnitValueComboBox(gpuCurrentMem);

		add(new JLabel("GPU Type"), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		add(garch, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		
		add(new JLabel("GPUs/node"),
			new GridBagConstraints(2, 0, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(gpuCountPerNode, new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		
		add(new JLabel("Memory/node"), new GridBagConstraints(4, 0, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		add(upperBound, new GridBagConstraints(5, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		add(upperBoundUnit, new GridBagConstraints(6, 0, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		
	}

	private class gpuArchListener implements ItemListener
	{
		private JobDocument _doc;

		private gpuArchListener(JobDocument doc)
		{
			_doc = doc;
		}

		@Override
		public void itemStateChanged(ItemEvent e)
		{
			if (e.getStateChange() == ItemEvent.SELECTED)
				_doc.gpuArchitecture((GPUProcessorArchitecture) e.getItem());
			else
				_doc.gpuArchitecture(null);
		}
	}
}