package edu.virginia.vcgr.genii.client.cmd.tools.besmgr;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import org.morgan.util.gui.progress.DefaultProgressNotifier;
import org.morgan.util.gui.progress.ProgressListener;
import org.morgan.util.gui.progress.ProgressMonitor;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ICallingContext;


public class BESManagerPanel extends JPanel
{
	static final long serialVersionUID = 0L;
	
	private ICallingContext _callingContext;
	private EndpointReferenceType _target;
	private ApplyAction _applyAction = new ApplyAction();
	
	private ManagementData _data;
	
	private JComboBox _screenSaverInactiveCombo = new JComboBox(
		BESPolicyActionWrapper.values());
	private JComboBox _userLoggedInCombo = new JComboBox(
		BESPolicyActionWrapper.values());
	private JTextField _thresholdField = new JTextField();
	private JCheckBox _acceptNewActivitiesCheckBox = new JCheckBox(
		"Currently Accepting New Activities");
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
		_acceptNewActivitiesCheckBox.setEnabled(false);
		_applyAction.setEnabled(false);
		_statusLabel.setText("Acquiring current information.");
		
		ProgressMonitor<ManagementData> monitor =
			new ProgressMonitor<ManagementData>();
		
		monitor.addProgressListener(new AcquireFinishProgressListener(), true);
		monitor.addProgressNotifier(new DefaultProgressNotifier(
			this, "Query BES Container", 
			"Getting resource properties from BES.", 1000L), false);
		
		monitor.startTask(new BESManagerAcquisitionTask(_callingContext, _target));
	}
	
	public BESManagerPanel(ICallingContext callingContext, 
		EndpointReferenceType target)
	{
		super(new GridBagLayout());
		
		_callingContext = callingContext;
		_target = target;
		
		add(createActivityPoliciesPanel(), new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		add(_acceptNewActivitiesCheckBox, new GridBagConstraints(
			0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(new JButton(_applyAction), new GridBagConstraints(
			0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_statusLabel, new GridBagConstraints(
			0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
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
			// TODO
			System.err.println("The apply button was clicked.");
		}
	}
	
	private class AcquireFinishProgressListener 
		implements ProgressListener<ManagementData>
	{
		@Override
		public void taskCancelled()
		{
			System.err.println("Cancelled.");
			System.exit(1);
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
			_acceptNewActivitiesCheckBox.setSelected(
				_data.isAcceptingNewActivities());
			
			_screenSaverInactiveCombo.setEnabled(true);
			_userLoggedInCombo.setEnabled(true);
			_thresholdField.setEnabled(true);
			_acceptNewActivitiesCheckBox.setEnabled(true);
		}

		@Override
		public void taskExcepted(Exception e)
		{
			System.err.println("Task excepted.");
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}