package edu.virginia.vcgr.genii.ui.plugins.acct;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.acct.AccountingCredentialTypes;

class CredentialModel extends AbstractTableModel
{
	static final long serialVersionUID = 0L;

	private Vector<CredentialBundle> _bundles;

	CredentialModel(Connection connection) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT cid, credentialtype, credentialdesc " + "FROM xcgcredentials");
			rs = stmt.executeQuery();

			_bundles = new Vector<CredentialBundle>();
			while (rs.next()) {
				AccountingCredentialTypes type = null;
				try {
					type = AccountingCredentialTypes.valueOf(rs.getString(2));
				} catch (Throwable cause) {
				}

				_bundles.add(new CredentialBundle(rs.getLong(1), type, rs.getString(3)));
			}
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	@Override
	public int getColumnCount()
	{
		return 2;
	}

	@Override
	public int getRowCount()
	{
		return _bundles.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		CredentialBundle bundle = _bundles.get(rowIndex);
		switch (columnIndex) {
			case 0:
				return bundle.credentialType();
			case 1:
				return bundle;
		}

		return null;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		switch (columnIndex) {
			case 0:
				return AccountingCredentialTypes.class;
			case 1:
				return CredentialBundle.class;
		}

		return null;
	}

	@Override
	public String getColumnName(int column)
	{
		switch (column) {
			case 0:
				return "Credential Type";
			case 1:
				return "Credential Description";
		}

		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnIndex == 0;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		if (columnIndex == 0) {
			_bundles.get(rowIndex).credentialType((AccountingCredentialTypes) aValue);

			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}

	final Collection<CredentialBundle> dirtyBundles()
	{
		Collection<CredentialBundle> ret = new LinkedList<CredentialBundle>();
		for (CredentialBundle bundle : _bundles)
			if (bundle.isDirty())
				ret.add(bundle);

		return ret;
	}
}