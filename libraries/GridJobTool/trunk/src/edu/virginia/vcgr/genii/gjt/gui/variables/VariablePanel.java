package edu.virginia.vcgr.genii.gjt.gui.variables;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableListener;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableManager;

public class VariablePanel extends JPanel {
	static final long serialVersionUID = 0L;

	private JTabbedPane _parent;

	private void handleTabEnabling(VariableManager mgr) {
		int index = _parent.indexOfComponent(this);
		setEnabled(!mgr.variables().isEmpty());
		if (index >= 0)
			_parent.setEnabledAt(index, isEnabled());
	}

	public VariablePanel(JTabbedPane parent, JobDocumentContext context) {
		super(new GridBagLayout());

		_parent = parent;
		setName("Grid Job Variables");

		context.variableManager().addVariableListener(
				new VariableListenerImpl());

		add(new JScrollPane(new VariableTable(context)),
				new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
						GridBagConstraints.NORTH,
						GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
						5, 5));

		handleTabEnabling(context.variableManager());
	}

	private class VariableListenerImpl implements VariableListener {
		@Override
		public void variableAdded(VariableManager manager, String variableName) {
			handleTabEnabling(manager);
		}

		@Override
		public void variableRemoved(VariableManager manager, String variableName) {
			handleTabEnabling(manager);
		}
	}
}