package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.utils.flock.FileLockException;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;

class ExportDataAction extends AbstractAction
{
	static final long serialVersionUID = 0L;

	static final private String _EXPORT_DATA_BUTTON = "Export Data";

	private ExportDirDialog _owner;
	private String _ContainerPath;
	private String _TargetPath;
	private Collection<IExportChangeListener> _listeners = new ArrayList<IExportChangeListener>();

	public ExportDataAction(ExportDirDialog owner, String ContainerPath, String TargetPath)
	{
		super(_EXPORT_DATA_BUTTON);

		_owner = owner;
		_ContainerPath = ContainerPath;
		_TargetPath = TargetPath;
	}

	public void addExportChangeListener(IExportChangeListener listener)
	{
		_listeners.add(listener);
	}

	public void removeExportChangeListener(IExportChangeListener listener)
	{
		_listeners.remove(listener);
	}

	protected void fireExportChanged()
	{
		for (IExportChangeListener listener : _listeners) {
			listener.exportsUpdated();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		try {
			ExportCreationDialog creation = new ExportCreationDialog(_owner, _ContainerPath, _TargetPath);
			creation.setModalityType(ModalityType.APPLICATION_MODAL);
			creation.pack();
			GuiUtils.centerComponent(creation);

			creation.setVisible(true);
			ExportCreationInformation creationInfo = creation.getExportCreationInformation();
			if (creationInfo != null) {
				createExport(creationInfo);
			}
		} catch (Throwable cause) {
			GuiUtils.displayError(_owner, "Export Error", cause);
		}
	}

	private void createExport(ExportCreationInformation creationInfo) throws FileLockException, IOException, ExportException,
		RNSException, CreationException, ResourceCreationFaultType, RemoteException, InvalidToolUsageException
	{
		String rnsPath = creationInfo.getRNSPath();
		RNSPath rPath =
			ExportManipulator.createExport(creationInfo.getContainerPath(), creationInfo.getLocalPath(), rnsPath,
				creationInfo.isLightWeight());
		ExportDirState.addExport(creationInfo.getContainerPath(),
			new ExportDirInformation(rPath, new File(creationInfo.getLocalPath())));
		fireExportChanged();
	}
}