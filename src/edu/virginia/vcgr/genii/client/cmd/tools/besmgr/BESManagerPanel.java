package edu.virginia.vcgr.genii.client.cmd.tools.besmgr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.gui.progress.DefaultProgressNotifier;
import org.morgan.util.gui.progress.ProgressListener;
import org.morgan.util.gui.progress.ProgressMonitor;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.container.bes.BESPolicy;

public class BESManagerPanel extends JPanel
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(BESManagerPanel.class);
	
	private ICallingContext _callingContext;
	private EndpointReferenceType _target;
	private ApplyAction _applyAction = new ApplyAction();
	private RefreshAction _refreshAction = new RefreshAction();
	
	private ManagementData _data;
	
	private String _lastThresholdString = "";
	private JComboBox _screenSaverInactiveCombo = new JComboBox(
		BESPolicyActionWrapper.values());
	private JComboBox _userLoggedInCombo = new JComboBox(
		BESPolicyActionWrapper.values());
	private JTextField _thresholdField = new JTextField(8);
	private JCheckBox _isAccepting = new JCheckBox("Accept New Activities");
	private JLabel _statusLabel = new JLabel(" ");
	
	private JPanel createActivityPoliciesPanel()
	{
		JPanel ret = new JPanel(new GridBagLayout());
		
		ret.add(new JLabel("Screen Saver Inactive"),
			new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		ret.add(_screenSaverInactiveCombo, 
			new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		ret.add(new JLabel("User Logged In"),
			new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		ret.add(_userLoggedInCombo, 
			new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		ret.add(new JLabel("Activity Threshold"),
			new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		ret.add(_thresholdField, 
			new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		
		ret.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
			"Activity Policies"));
		
		return ret;
	}
	
	private void acquireState()
	{
		_screenSaverInactiveCombo.setEnabled(false);
		_userLoggedInCombo.setEnabled(false);
		_thresholdField.setEnabled(false);
		_isAccepting.setEnabled(false);
		_applyAction.setEnabled(false);
		_refreshAction.setEnabled(false);
		_statusLabel.setText("Acquiring current information.");
		
		ProgressMonitor<ManagementData> monitor =
			new ProgressMonitor<ManagementData>();
		
		monitor.addProgressListener(new AcquireFinishProgressListener(), true);
		monitor.addProgressNotifier(new DefaultProgressNotifier(
			this, "Query BES Container", 
			"Getting resource properties from BES.", 1000L), false);
		
		monitor.startTask(new BESManagerAcquisitionTask(_callingContext, _target));
	}
	
	private void applyState()
	{
		Integer threshold = null;
		String text = _thresholdField.getText().trim();
		if (text.length() != 0)
		{
			try 
			{
				threshold = Integer.valueOf(text);
				if (threshold < 0)
					throw new NumberFormatException();
			}
			catch (NumberFormatException nfe) 
			{
				JOptionPane.showMessageDialog(this,
					"Threshold value is invalid.",
					"Invalid Threshold Value", JOptionPane.ERROR_MESSAGE);
			}
		}
		_screenSaverInactiveCombo.setEnabled(false);
		_userLoggedInCombo.setEnabled(false);
		_thresholdField.setEnabled(false);
		_isAccepting.setEnabled(false);
		_applyAction.setEnabled(false);
		_refreshAction.setEnabled(false);
		_statusLabel.setText("Storing new configuration.");
		
		ProgressMonitor<ManagementData> monitor =
			new ProgressMonitor<ManagementData>();
		
		monitor.addProgressListener(new ApplyFinishProgressListener(), true);
		monitor.addProgressNotifier(new DefaultProgressNotifier(
			this, "Applying BES Configuration", 
			"Applying new BES configuration.", 1000L), false);
		
		monitor.startTask(new BESManagerConfigurationTask(
			_callingContext, _target, new ManagementData(
				new BESPolicy(
					((BESPolicyActionWrapper)_userLoggedInCombo.getSelectedItem()).action(),
					((BESPolicyActionWrapper)_screenSaverInactiveCombo.getSelectedItem()).action()),
				threshold, _isAccepting.isSelected())));
	}
	
	public BESManagerPanel(ICallingContext callingContext, 
		EndpointReferenceType target)
	{
		super(new GridBagLayout());
		
		_callingContext = callingContext;
		_target = target;
		
		ChangeListener changeListener = new ChangeListener();
		
		_userLoggedInCombo.addActionListener(changeListener);
		_screenSaverInactiveCombo.addActionListener(changeListener);
		_thresholdField.addCaretListener(changeListener);
		_isAccepting.addActionListener(changeListener);
		
		add(createActivityPoliciesPanel(), new GridBagConstraints(
			0, 0, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		add(_isAccepting, new GridBagConstraints(
			0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(new JButton(_applyAction), new GridBagConstraints(
			0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(new JButton(_refreshAction), new GridBagConstraints(
			1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_statusLabel, new GridBagConstraints(
			0, 3, 2, 1, 1.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		
		acquireState();
	}
	
	private class ApplyAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		public ApplyAction()
		{
			super("Apply");
			
			setEnabled(false);
		}
		
		@Override
		public void actionPerformed(ActionEvent event)
		{
			applyState();
		}
	}
	
	private class RefreshAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		public RefreshAction()
		{
			super("Refresh");
			
			setEnabled(false);
		}
		
		@Override
		public void actionPerformed(ActionEvent event)
		{
			acquireState();
		}
	}
	
	private class AcquireFinishProgressListener 
		implements ProgressListener<ManagementData>
	{
		@Override
		public void taskCancelled()
		{
			_statusLabel.setText("Cancelled");
			
			_screenSaverInactiveCombo.setEnabled(false);
			_userLoggedInCombo.setEnabled(false);
			_thresholdField.setEnabled(false);
			_isAccepting.setEnabled(false);
			_applyAction.setEnabled(false);
			_refreshAction.setEnabled(true);
		}

		@Override
		public void taskCompleted(ManagementData result)
		{
			_data = result;
			_statusLabel.setText(" ");
			
			_screenSaverInactiveCombo.setSelectedItem(
				BESPolicyActionWrapper.wrap(
					_data.policy().getScreenSaverInactiveAction()));
			_userLoggedInCombo.setSelectedItem(
				BESPolicyActionWrapper.wrap(
					_data.policy().getUserLoggedInAction()));
			_thresholdField.setText(
				_data.threshold() == null ? "" : _data.threshold().toString());
			_isAccepting.setSelected(_data.isAcceptingActivities());
			_lastThresholdString = _thresholdField.getText();
			
			_screenSaverInactiveCombo.setEnabled(true);
			_userLoggedInCombo.setEnabled(true);
			_thresholdField.setEnabled(true);
			_isAccepting.setEnabled(true);
			_applyAction.setEnabled(false);
			_refreshAction.setEnabled(true);
		}

		@Override
		public void taskExcepted(Exception e)
		{
			_statusLabel.setText("Error");
			
			_screenSaverInactiveCombo.setEnabled(false);
			_userLoggedInCombo.setEnabled(false);
			_thresholdField.setEnabled(false);
			_isAccepting.setEnabled(false);
			_applyAction.setEnabled(false);
			_refreshAction.setEnabled(true);
			
			_logger.error("Unable to refresh BES information.", e);
			JOptionPane.showMessageDialog(BESManagerPanel.this, 
				"An error occurred while refreshing BES information.",
				"BES Refresh Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private class ApplyFinishProgressListener 
		implements ProgressListener<ManagementData>
	{

		@Override
		public void taskCancelled()
		{
			_statusLabel.setText("Cancelled");
			
			_screenSaverInactiveCombo.setEnabled(false);
			_userLoggedInCombo.setEnabled(false);
			_thresholdField.setEnabled(false);
			_isAccepting.setEnabled(false);
			_applyAction.setEnabled(false);
			_refreshAction.setEnabled(true);
			
			acquireState();
		}

		@Override
		public void taskCompleted(ManagementData result)
		{
			_data = result;
			_statusLabel.setText(" ");
			
			_screenSaverInactiveCombo.setSelectedItem(
				BESPolicyActionWrapper.wrap(
					_data.policy().getScreenSaverInactiveAction()));
			_userLoggedInCombo.setSelectedItem(
				BESPolicyActionWrapper.wrap(
					_data.policy().getUserLoggedInAction()));
			_thresholdField.setText(
				_data.threshold() == null ? "" : _data.threshold().toString());
			_isAccepting.setSelected(_data.isAcceptingActivities());
			_lastThresholdString = _thresholdField.getText();
			
			_screenSaverInactiveCombo.setEnabled(true);
			_userLoggedInCombo.setEnabled(true);
			_thresholdField.setEnabled(true);
			_isAccepting.setEnabled(true);
			_applyAction.setEnabled(false);
			_refreshAction.setEnabled(true);
		}

		@Override
		public void taskExcepted(Exception e)
		{
			_statusLabel.setText("Error");
			
			_screenSaverInactiveCombo.setEnabled(true);
			_userLoggedInCombo.setEnabled(true);
			_thresholdField.setEnabled(true);
			_isAccepting.setEnabled(true);
			_applyAction.setEnabled(true);
			_refreshAction.setEnabled(true);
			
			_logger.error("Unable to configure BES.", e);
			JOptionPane.showMessageDialog(BESManagerPanel.this, 
				"An error occurred while configuration BES container.",
				"BES Configuration Error", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	private class ChangeListener
		implements ActionListener, CaretListener
	{
		@Override
		public void actionPerformed(ActionEvent event)
		{
			_applyAction.setEnabled(true);
		}

		@Override
		public void caretUpdate(CaretEvent e)
		{
			String newThresholdString = _thresholdField.getText();
			if (!_lastThresholdString.equals(newThresholdString))
			{
				_lastThresholdString = newThresholdString;
				_applyAction.setEnabled(true);
			}
		}
	}
}