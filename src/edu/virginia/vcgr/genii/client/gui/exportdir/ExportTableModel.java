package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import edu.virginia.vcgr.genii.client.utils.flock.FileLockException;

public class ExportTableModel extends AbstractTableModel implements IExportChangeListener
{
	static final long serialVersionUID = 0L;
	
	static final private String []_COLUMN_NAMES = new String[] { "Deployment", "Local Path", "RNS Path" };
	
	static private class RowData 
	{
		private String _deploymentName;
		private ExportDirInformation _exportInfo;
		
		public RowData(String deploymentName, ExportDirInformation exportInfo)
		{
			_deploymentName = deploymentName;
			_exportInfo = exportInfo;
		}
		
		public String getDeploymentName()
		{
			return _deploymentName;
		}
		
		public ExportDirInformation getExportDirInformation()
		{
			return _exportInfo;
		}
	}
	
	private ArrayList<RowData> _rowData;
	
	public ExportTableModel() throws FileLockException
	{
		HashMap<String, Collection<ExportDirInformation>> exports = ExportDirState.getKnownExports();
		_rowData = new ArrayList<RowData>();
		
		for (String deploymentName : exports.keySet())
		{
			for (ExportDirInformation eInfo : exports.get(deploymentName))
			{
				_rowData.add(new RowData(deploymentName, eInfo));
			}
		}
	}
	
	@Override
	public int getColumnCount()
	{
		return _COLUMN_NAMES.length;
	}

	@Override
	public int getRowCount()
	{
		return _rowData.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		RowData data = _rowData.get(rowIndex);
		switch (columnIndex)
		{
			case 0 :
				return data.getDeploymentName();
			case 1 :
				return data.getExportDirInformation().getLocalPath();
			case 2 :
				return data.getExportDirInformation().getRNSPath();
			default :
					return null;
		}
	}
	
	@Override
	public Class<?> getColumnClass(int index)
	{
		return String.class;
	}
	
	@Override
	public String getColumnName(int index)
	{
		return _COLUMN_NAMES[index];
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int colIndex)
	{
		return false;
	}

	@Override
	public void exportsUpdated()
	{
		try
		{
			HashMap<String, Collection<ExportDirInformation>> exports = ExportDirState.getKnownExports();
			_rowData = new ArrayList<RowData>();
			
			for (String deploymentName : exports.keySet())
			{
				for (ExportDirInformation eInfo : exports.get(deploymentName))
				{
					_rowData.add(new RowData(deploymentName, eInfo));
				}
			}
			
			fireTableDataChanged();
		}
		catch (FileLockException fle)
		{
			JOptionPane.showMessageDialog(null, fle.getLocalizedMessage(), "Export Exception", 
				JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public ExportDirInformation getRow(int rowNumber)
	{
		return _rowData.get(rowNumber).getExportDirInformation();
	}
}