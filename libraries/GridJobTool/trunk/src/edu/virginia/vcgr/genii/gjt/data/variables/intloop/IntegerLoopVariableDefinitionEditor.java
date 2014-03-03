package edu.virginia.vcgr.genii.gjt.data.variables.intloop;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinitionEditor;
import edu.virginia.vcgr.genii.gjt.gui.util.ButtonPanel;

public class IntegerLoopVariableDefinitionEditor extends VariableDefinitionEditor<IntegerLoopVariableDefinition>
{
	static final long serialVersionUID = 0L;

	private SpinnerNumberModel _start = new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
	private SpinnerNumberModel _stop = new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
	private SpinnerNumberModel _step = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);

	@Override
	public IntegerLoopVariableDefinition getVariableDefinitionImpl()
	{
		return new IntegerLoopVariableDefinition(_start.getNumber().intValue(), _stop.getNumber().intValue(), _step.getNumber()
			.intValue());
	}

	public IntegerLoopVariableDefinitionEditor(Window owner)
	{
		super(owner, "Integer Range Editor");

		JSpinner start = new JSpinner(_start);
		JSpinner stop = new JSpinner(_stop);
		JSpinner step = new JSpinner(_step);

		start.addChangeListener(new ChangeListenerImpl());
		stop.addChangeListener(new ChangeListenerImpl());

		Container container = getContentPane();
		container.setLayout(new GridBagLayout());

		container.add(new JLabel("Start Value"), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		container.add(start, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

		container.add(new JLabel("End Value"), new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		container.add(stop, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

		container.add(new JLabel("Step By"), new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		container.add(step, new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

		container.add(ButtonPanel.createHorizontalPanel(new OKAction(), new CancelAction()), new GridBagConstraints(0, 3, 2, 1,
			1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
	}

	@Override
	public void setFromVariableDefinition(IntegerLoopVariableDefinition variableDefinition)
	{
		_start.setValue(new Integer(variableDefinition._start));
		_stop.setValue(new Integer(variableDefinition._end));
		_step.setValue(new Integer(variableDefinition._step));
	}

	private class OKAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private OKAction()
		{
			super("OK");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			accept();
		}
	}

	private class CancelAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private CancelAction()
		{
			super("Cancel");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			cancel();
		}
	}

	private class ChangeListenerImpl implements ChangeListener
	{
		@Override
		public void stateChanged(ChangeEvent e)
		{
			JSpinner spinner = (JSpinner) e.getSource();

			int startValue = ((Integer) _start.getValue()).intValue();
			int stopValue = ((Integer) _stop.getValue()).intValue();

			if (startValue <= stopValue)
				return;

			int value = ((Integer) spinner.getValue()).intValue();
			if (value == startValue) {
				// Start changed
				_stop.setValue(new Integer(startValue));
			} else {
				// Stop changed
				_start.setValue(new Integer(stopValue));
			}
		}
	}
}