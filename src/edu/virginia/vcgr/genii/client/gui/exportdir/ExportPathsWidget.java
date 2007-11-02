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

public class ExportPathsWidget extends JComponent
{
	static final long serialVersionUID = 0L;
	
	static private final String _LOCAL_LABEL = "Local Path";
	static private final String _RNS_LABEL = "RNS Path";
	static private final String _BUTTON_LABEL = "Browse";
	
	private JTextField _localPath;
	private JTextField _rnsPath;
	private Collection<IInformationListener> _listeners = new ArrayList<IInformationListener>();
	
	public ExportPathsWidget()
	{
		super();
		
		setLayout(new GridBagLayout());
		add(new JLabel(_LOCAL_LABEL),
			new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		add(_localPath = new JTextField(),
			new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		add(new JButton(new BrowseLocalPathAction(this, _BUTTON_LABEL, _localPath)),
			new GridBagConstraints(2, 0, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel(_RNS_LABEL),
			new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		add(_rnsPath = new JTextField(),
			new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		add(new JButton(new BrowseRNSPathAction(null, _BUTTON_LABEL, _rnsPath)),
			new GridBagConstraints(2, 1, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		
		_localPath.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e)
			{
				fireInformationUpdated();
			}
		});
		_rnsPath.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e)
			{
				fireInformationUpdated();
			}
		});
		
		Dimension d = new Dimension(400, 50);
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
		for (IInformationListener listener : _listeners)
		{
			listener.updateInformation();
		}
	}
	
	public String getLocalPath()
	{
		return _localPath.getText();
	}

	public String getRNSPath()
	{
		return _rnsPath.getText();
	}
}