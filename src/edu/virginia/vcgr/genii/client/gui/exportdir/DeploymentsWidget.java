package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import edu.virginia.vcgr.genii.client.install.ContainerInformation;
import edu.virginia.vcgr.genii.client.install.InstallationState;
import edu.virginia.vcgr.genii.client.utils.flock.FileLockException;

@SuppressWarnings("rawtypes")
public class DeploymentsWidget extends JComponent
{
	static final long serialVersionUID = 0L;
	
	static final private String _DEPLOYMENTS = "Deployment";
	
	private JComboBox _comboBox;
	private Collection<IInformationListener> _listeners = new ArrayList<IInformationListener>();
	
	@SuppressWarnings("unchecked")
    public DeploymentsWidget() throws FileLockException, NoContainersException
	{
		super();
		
		setLayout(new GridBagLayout());
		add(new JLabel(_DEPLOYMENTS), new GridBagConstraints(
			0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		
		Vector<ContainerInformation> containers = new Vector<ContainerInformation>();
		
		for (ContainerInformation containerInfo : InstallationState.getRunningContainers().values())
		{
			containers.add(containerInfo);
		}
		
		if (containers.size() == 0)
		{
			throw new NoContainersException();
		}
		
		_comboBox = new JComboBox(containers);
		add(_comboBox, new GridBagConstraints(
			1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		
		_comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				fireInformationUpdated();
			}
		});
	}
	
	public void addInformationListener(IInformationListener listener)
	{
		_listeners.add(listener);
	}
	
	public void removeInformationListener(IInformationListener listener)
	{
		_listeners.remove(listener);
	}
	
	protected void fireInformationUpdated()
	{
		for (IInformationListener listener : _listeners)
		{
			listener.updateInformation();
		}
	}
	
	public ContainerInformation getSelectedDeployment()
	{
		return (ContainerInformation)_comboBox.getSelectedItem();
	}
}