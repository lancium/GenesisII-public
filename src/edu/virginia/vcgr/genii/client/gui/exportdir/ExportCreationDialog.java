package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;

import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.install.ContainerInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.utils.flock.FileLockException;

public class ExportCreationDialog extends JDialog
{
	static final long serialVersionUID = 0L;
	
	static private final String _TITLE = "Export Creation";
	
	private ExportPathsWidget _paths;
	private DeploymentsWidget _deployments;
	private ExportCreationInformation _information = null;
	
	public ExportCreationDialog(JDialog owner)
		throws FileLockException, NoContainersException
	{
		super(owner);
		
		Container container;
				
		setTitle(_TITLE);
		container = getContentPane();
		
		container.setLayout(new GridBagLayout());
		container.add(_deployments = new DeploymentsWidget(),
			new GridBagConstraints(0, GridBagConstraints.RELATIVE, 2, 1,
				1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		container.add(_paths= new ExportPathsWidget(),
			new GridBagConstraints(0, GridBagConstraints.RELATIVE, 2, 1,
				1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		
		CreateExportAction action = new CreateExportAction();
		_deployments.addInformationListener(action);
		_paths.addInformationListener(action);
		
		container.add(new JButton(action),
			new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		container.add(new JButton(new CancelAction()),
				new GridBagConstraints(1, GridBagConstraints.RELATIVE, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
	}
	
	private boolean hasEnoughInformationToCreate()
	{
		ContainerInformation containerInfo = _deployments.getSelectedDeployment();
		String localPath = _paths.getLocalPath();
		String rnsPath = _paths.getRNSPath();
		
		return containerInfo != null && localPath != null && localPath.trim().length() > 0 && rnsPath != null
			&& rnsPath.trim().length() > 0;
	}
	
	public ExportCreationInformation getExportCreationInformation()
	{
		return _information;
	}
	
	private class CreateExportAction extends AbstractAction
		implements IInformationListener
	{
		static final long serialVersionUID = 0L;
		
		public CreateExportAction()
		{
			super("Create Export");
			
			setEnabled(hasEnoughInformationToCreate());
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				File localPath = new File(_paths.getLocalPath());
				ExportManipulator.validate(localPath);
				RNSPath rnsPath = RNSPath.getCurrent().lookup(_paths.getRNSPath(), RNSPathQueryFlags.MUST_NOT_EXIST);
				ExportManipulator.validate(rnsPath);
				_information = new ExportCreationInformation(_deployments.getSelectedDeployment(),
					_paths.getLocalPath(), _paths.getRNSPath());
				setVisible(false);
			}
			catch (Throwable cause)
			{
				GuiUtils.displayError((Component)e.getSource(), "Export Creation Error", cause);
			}
		}

		@Override
		public void updateInformation()
		{
			setEnabled(hasEnoughInformationToCreate());
		}
	}
	
	private class CancelAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		public CancelAction()
		{
			super("Cancel");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			_information = null;
			setVisible(false);
		}
	}
}