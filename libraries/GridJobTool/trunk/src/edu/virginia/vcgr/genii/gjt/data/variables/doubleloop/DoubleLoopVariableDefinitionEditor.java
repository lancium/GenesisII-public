package edu.virginia.vcgr.genii.gjt.data.variables.doubleloop;

import java.awt.Container;
import java.awt.Dimension;
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

public class DoubleLoopVariableDefinitionEditor extends
		VariableDefinitionEditor<DoubleLoopVariableDefinition> {
	static final long serialVersionUID = 0L;

	static private final Dimension SIZE = new Dimension(250, 200);

	private SpinnerNumberModel _start = new SpinnerNumberModel(0.0, -1
			* Double.MAX_VALUE, Double.MAX_VALUE, 0.1);
	private SpinnerNumberModel _stop = new SpinnerNumberModel(0.0, -1
			* Double.MAX_VALUE, Double.MAX_VALUE, 0.1);
	private SpinnerNumberModel _step = new SpinnerNumberModel(0.1,
			Double.MIN_VALUE, Double.MAX_VALUE, 0.1);

	@Override
	public DoubleLoopVariableDefinition getVariableDefinitionImpl() {
		return new DoubleLoopVariableDefinition(_start.getNumber()
				.doubleValue(), _stop.getNumber().doubleValue(), _step
				.getNumber().doubleValue());
	}

	public DoubleLoopVariableDefinitionEditor(Window owner) {
		super(owner, "Double Range Editor");

		JSpinner start = new JSpinner(_start);
		JSpinner stop = new JSpinner(_stop);
		JSpinner step = new JSpinner(_step);

		start.addChangeListener(new ChangeListenerImpl());
		stop.addChangeListener(new ChangeListenerImpl());

		Container container = getContentPane();
		container.setLayout(new GridBagLayout());

		container.add(new JLabel("Start Value"), new GridBagConstraints(0, 0,
				1, 1, 0.0, 1.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		container.add(start, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));

		container.add(new JLabel("End Value"), new GridBagConstraints(0, 1, 1,
				1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		container.add(stop, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));

		container.add(new JLabel("Step By"), new GridBagConstraints(0, 2, 1, 1,
				0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		container.add(step, new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));

		container.add(ButtonPanel.createHorizontalPanel(new OKAction(),
				new CancelAction()), new GridBagConstraints(0, 3, 2, 1, 1.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));

		setPreferredSize(SIZE);
	}

	@Override
	public void setFromVariableDefinition(
			DoubleLoopVariableDefinition variableDefinition) {
		_start.setValue(new Double(variableDefinition._start));
		_stop.setValue(new Double(variableDefinition._end));
		_step.setValue(new Double(variableDefinition._step));
	}

	private class OKAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private OKAction() {
			super("OK");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			accept();
		}
	}

	private class CancelAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private CancelAction() {
			super("Cancel");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			cancel();
		}
	}

	private class ChangeListenerImpl implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			JSpinner spinner = (JSpinner) e.getSource();

			double startValue = ((Double) _start.getValue()).doubleValue();
			double stopValue = ((Double) _stop.getValue()).doubleValue();

			if (startValue <= stopValue)
				return;

			double value = ((Double) spinner.getValue()).doubleValue();
			if (value == startValue) {
				// Start changed
				_stop.setValue(new Double(startValue));
			} else {
				// Stop changed
				_start.setValue(new Double(stopValue));
			}
		}
	}
}