package edu.virginia.vcgr.genii.ui.plugins.acct;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import org.morgan.utils.gui.ButtonPanel;

import edu.virginia.vcgr.genii.client.acct.AccountingCredentialTypes;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.ui.utils.ecombo.EnumComboBox;
import edu.virginia.vcgr.genii.ui.utils.ecombo.EnumComboSort;

class CredentialDialog extends JDialog
{
	static final long serialVersionUID = 0L;
	
	private CredentialModel _model;
	private Collection<CredentialBundle> _dirtyBundles = null;
	
	private CredentialDialog(Component ownerComponent,
		Connection connection) throws SQLException
	{
		super(SwingUtilities.getWindowAncestor(ownerComponent),
			"Accounting Credential Manager");
	
		Container container = getContentPane();
		container.setLayout(new GridBagLayout());
		
		_model = new CredentialModel(connection);
		JTable table = new JTable(_model);
		
		container.add(new JScrollPane(table), new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		container.add(ButtonPanel.createHorizontalButtonPanel(
			new CommitAction(), new CancelAction()), new GridBagConstraints(
				0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		TableColumn c = table.getColumnModel().getColumn(1);
		c.setMinWidth(400);
		c.setPreferredWidth(c.getMinWidth());
		
		c = table.getColumnModel().getColumn(0);
		c.setPreferredWidth(64);
		
		EnumComboBox<AccountingCredentialTypes> box =
			new EnumComboBox<AccountingCredentialTypes>(
				AccountingCredentialTypes.class, EnumComboSort.Alphabetically, 
				true, null);
		
		c.setCellEditor(new DefaultCellEditor(box));
	}
	
	private class CommitAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private CommitAction()
		{
			super("Commit Changes");
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			_dirtyBundles = _model.dirtyBundles();
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
	
	static Collection<CredentialBundle> manageCredentials(
		Component owner, Connection connection) throws SQLException
	{
		CredentialDialog dialog = new CredentialDialog(owner, connection);
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.pack();
		GuiUtils.centerComponent(dialog);
		dialog.setVisible(true);
		return dialog._dirtyBundles;
	}
}