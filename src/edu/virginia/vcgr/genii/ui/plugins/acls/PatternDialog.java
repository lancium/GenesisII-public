package edu.virginia.vcgr.genii.ui.plugins.acls;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.security.auth.x500.X500Principal;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.credentials.identity.X509Identity;
import edu.virginia.vcgr.genii.ui.UIContext;

class PatternDialog extends JDialog
{
	static final long serialVersionUID = 0L;
	
	private X500Principal _currentPattern = null;
	private X500Principal _selectedPattern = null;

	private PatternDialog(Window owner, X509Identity issuingAuthority)
	{
		super(owner);
		setTitle("Certificate Pattern ACL");
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		OIDNameTable table = new OIDNameTable();
		CurrentPrincipalLabel label = new CurrentPrincipalLabel();
		table.getModel().addTableModelListener(label);
		
		content.add(new JLabel(String.format(
			"Pattern for certificates created by \"%s\".",
			issuingAuthority.describe(VerbosityLevel.LOW))),
			new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JScrollPane(table),
			new GridBagConstraints(
			0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH,
			new Insets(5, 5, 5, 5), 5, 5));
		content.add(label, new GridBagConstraints(
			0, 2, 2, 1, 1.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new SetPatternAction()),
			new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new CancelAction()),
			new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	private class SetPatternAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private SetPatternAction()
		{
			super("Accept Pattern");
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			_selectedPattern = _currentPattern;
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
			dispose();
		}
	}
	
	private class CurrentPrincipalLabel extends JLabel
		implements TableModelListener
	{
		static final long serialVersionUID = 0L;
		
		private CurrentPrincipalLabel()
		{
			setLabel();
		}
		
		private void setLabel()
		{
			String label = "Current Principal:";
			
			if (_currentPattern != null)
				label += String.format("  %s", _currentPattern);
			
			setText(label);
		}

		@Override
		public void tableChanged(TableModelEvent e)
		{
			OIDNameTableModel model = (OIDNameTableModel)e.getSource();
			X500Principal principal = model.formPrincipal();
			_currentPattern = principal;
			setLabel();
		}
	}
	
	static X500Principal getPattern(UIContext context,
		X509Identity issuingAuthority,
		JComponent responsibleComponent)
	{
		PatternDialog pd = new PatternDialog(
			SwingUtilities.getWindowAncestor(responsibleComponent),
			issuingAuthority);
		pd.pack();
		pd.setLocationRelativeTo(responsibleComponent);
		pd.setModalityType(ModalityType.DOCUMENT_MODAL);
		pd.setVisible(true);
		return pd._selectedPattern;
	}
}