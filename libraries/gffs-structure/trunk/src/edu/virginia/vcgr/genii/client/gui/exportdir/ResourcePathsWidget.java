package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;

public class ResourcePathsWidget extends JComponent
{
	static final long serialVersionUID = 0L;

	static private final String _LOCAL_LABEL = "Local Path";
	static private final String _RNS_LABEL = "Target Path";
	static private final String _BUTTON_LABEL = "Browse";
	static private final String _CONTAINER_LABEL = "Container Path";
	static private final String _BROWSE_CONTAINER = "Chose the container on which to place the resource";
	static private final String _BROWSE_TARGET_PATH = "Chose the path name for your new resource";

	private JTextField _localPath;
	private JTextField _rnsPath;
	private JTextField _containerPath;
	private Collection<IInformationListener> _listeners = new ArrayList<IInformationListener>();

	private JButton createLocalBrowseButton()
	{
		if (OperatingSystemType.getCurrent().isWindows()) {
			// A persistent bug in Microsoft Windows JFileChooser
			// implementation can cause this widget to hang
			// indefinitely and so we can't safely use it.
			return null;
		}

		JButton browseLocal = new JButton(new BrowseLocalPathAction(this, _BUTTON_LABEL, _localPath));
		return browseLocal;
	}

	public ResourcePathsWidget(boolean getLocalPath, boolean getContainerPath, String ContainerPath, String TargetPath)
	{
		super();

		setLayout(new GridBagLayout());

		if (getLocalPath) {
			// Get the local path of the file or directory
		add(new JLabel(_LOCAL_LABEL), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_localPath = new JTextField(), new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		JButton browseLocal = createLocalBrowseButton();
		if (browseLocal != null)
			add(browseLocal, new GridBagConstraints(2, 0, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		}
		else {
			_localPath = new JTextField();
		}
		
			// Set up get RNS path for new resource
		add(new JLabel(_RNS_LABEL), new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_rnsPath = new JTextField(TargetPath), new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(new JButton(new BrowseRNSPathAction(null, _BUTTON_LABEL, _rnsPath, _BROWSE_TARGET_PATH)), new GridBagConstraints(2,
			1, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

			if (getContainerPath) {
			// Get RNS path of container on which the resource should reside
		add(new JLabel(_CONTAINER_LABEL), new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_containerPath = new JTextField(ContainerPath), new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(new JButton(new BrowseRNSPathAction(null, _BUTTON_LABEL, _containerPath, _BROWSE_CONTAINER)),
			new GridBagConstraints(2, 2, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5,
				5, 5), 5, 5));
			}
			else {
				_containerPath = new JTextField(ContainerPath);
			}

		_localPath.addCaretListener(new CaretListener()
		{
			@Override
			public void caretUpdate(CaretEvent e)
			{
				fireInformationUpdated();
			}
		});
		_rnsPath.addCaretListener(new CaretListener()
		{
			@Override
			public void caretUpdate(CaretEvent e)
			{
				fireInformationUpdated();
			}
		});

		_containerPath.addCaretListener(new CaretListener()
		{
			@Override
			public void caretUpdate(CaretEvent e)
			{
				fireInformationUpdated();
			}
		});

		Dimension d = new Dimension(400, 100);
		setPreferredSize(d);
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
		for (IInformationListener listener : _listeners) {
			listener.updateInformation();
		}
	}

	public String getLocalPath()
	{
		return _localPath.getText();
	}

	public String getContainerPath()
	{
		return _containerPath.getText();
	}
	
	public void setContainerPath(String path)
	{
		_containerPath.setText(path);;
	}

	
	public String getRNSPath()
	{
		return _rnsPath.getText();
	}
}