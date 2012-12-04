package edu.virginia.vcgr.genii.ui.plugins.cmdfunc;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.morgan.utils.gui.ButtonPanel;
import org.morgan.utils.gui.GUIUtils;

import edu.virginia.vcgr.genii.client.resource.JavaCommandFunction;

@SuppressWarnings("rawtypes")
class CommandFunctionChoiceDialog extends JDialog
{
	static final long serialVersionUID = 0L;
	
	private JComboBox _functions;
	private JavaCommandFunction _selectedFunction = null;
	
	@SuppressWarnings("unchecked")
    private CommandFunctionChoiceDialog(Component ownerComponent,
		Collection<JavaCommandFunction> functions)
	{
		super(SwingUtilities.getWindowAncestor(ownerComponent),
			"Command Function Selection");
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		content.add(new JLabel("Which command function do you want to call?"),
			new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		
		_functions = new JComboBox(new Vector<JavaCommandFunction>(functions));
		content.add(_functions, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		
		content.add(ButtonPanel.createHorizontalButtonPanel(new OKAction(),
			new CancelAction()), new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}
	
	private class OKAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private OKAction()
		{
			super("Select");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			_selectedFunction =
				(JavaCommandFunction)_functions.getSelectedItem();
			dispose();
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
			_selectedFunction = null;
			dispose();
		}
	}
	
	static JavaCommandFunction chooseCommandFunction(
		Component ownerComponent, Collection<JavaCommandFunction> functions)
	{
		CommandFunctionChoiceDialog dialog = new CommandFunctionChoiceDialog(
			ownerComponent, functions);
		dialog.pack();
		GUIUtils.centerWindow(dialog);
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.setVisible(true);
		return dialog._selectedFunction;
	}
}