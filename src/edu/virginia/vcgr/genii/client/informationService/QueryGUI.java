/**
 * @author kkk5z
 * 
 * This is the class for the GUI used by the Information Service. It represent the 
 * properties from the AttributesDocument of the BES containers being monitored
 * by the information service.
 */
package edu.virginia.vcgr.genii.client.informationService;

import java.awt.event.*;

import javax.swing.*;

public class QueryGUI extends JFrame
{

	/**
	 * those are the labels and text fields that the GUI has
	 */
	protected static boolean readyToReturn = true;
	static GUIInternalStruct queryParameters = new GUIInternalStruct();

	private static final long serialVersionUID = 1L;
	private JLabel OperatingSystem;
	private JTextField OSType;
	private JLabel OSTypeLabel;
	private JTextField OSVersion;
	private JLabel OSVersionLabel;

	private JLabel CPU;
	private JLabel CPUArchitectureNameLabel;
	private JTextField CPUArchitectureName;
	private JLabel CPUCountLabel;
	private JTextField CPUCount;
	private JLabel CPUSpeedLabel;
	private JTextField CPUSpeed;

	private JLabel Memory;
	private JLabel PhysicalMamoryLabel;
	private JTextField PhysicalMemory;
	private JLabel VirtualMemoryLabel;
	private JTextField VirtualMemory;

	private JCheckBox IsAcceptingNewActivities;

	private JLabel CommonNameLabel;
	private JTextField CommonName;

	private JLabel TotalNumberOfActivitiesLabel;
	private JTextField TotalNumberOfActivities;

	private JLabel LocalResourceManagerTypeLabel;
	private JTextField LocalResourcemanagerType;

	private JLabel NamingProfileLabel;
	private JTextField NamingProfile;

	private JButton OKButton;
	private JButton CancelButton;

	public QueryGUI()
	{
		initComponents();
	}

	@SuppressWarnings("deprecation")
	/*
	 * Initializing the components of the GUI and setting up the layout
	 */
	private void initComponents()
	{
		OperatingSystem = new JLabel();
		OperatingSystem.setText("Operating System");

		OSType = new JTextField();
		OSTypeLabel = new JLabel();
		OSTypeLabel.setText("OS Type: ");
		OSVersion = new JTextField();
		OSVersionLabel = new JLabel();
		OSVersionLabel.setText("OS version: ");

		CPU = new JLabel();
		CPU.setText("CPU");
		CPUArchitectureNameLabel = new JLabel();
		CPUArchitectureNameLabel.setText("Architecture Name: ");
		CPUArchitectureName = new JTextField();
		CPUCountLabel = new JLabel();
		CPUCountLabel.setText("Count: ");
		CPUCount = new JTextField();
		CPUSpeedLabel = new JLabel();
		CPUSpeedLabel.setText("Speed: ");
		CPUSpeed = new JTextField();

		Memory = new JLabel();
		Memory.setText("Memory");
		PhysicalMamoryLabel = new JLabel();
		PhysicalMamoryLabel.setText("Physical: ");
		PhysicalMemory = new JTextField();
		PhysicalMemory.setText("");
		VirtualMemoryLabel = new JLabel();
		VirtualMemoryLabel.setText("Virtual: ");
		VirtualMemory = new JTextField();

		IsAcceptingNewActivities = new JCheckBox();
		IsAcceptingNewActivities.setLabel("IsAcceptingNewActivities");

		CommonNameLabel = new JLabel();
		CommonNameLabel.setText("CommonName: ");
		CommonName = new JTextField();

		TotalNumberOfActivitiesLabel = new JLabel();
		TotalNumberOfActivitiesLabel.setText("Total number of activities: ");
		TotalNumberOfActivities = new JTextField();

		LocalResourceManagerTypeLabel = new JLabel();
		LocalResourceManagerTypeLabel.setText("Local resource manager type: ");
		LocalResourcemanagerType = new JTextField();

		NamingProfileLabel = new JLabel();
		NamingProfileLabel.setText("Naming Profile: ");
		NamingProfile = new JTextField();

		OKButton = new JButton();
		CancelButton = new JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Information Service");

		OKButton.setText("OK");
		OKButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				submitButtonActionPerformed(evt);
				readyToReturn = false;

			}
		});

		CancelButton.setText("Cancel");
		CancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				submitButtonActionPerformed(evt);
				readyToReturn = false;
			}
		});

		// setting up the layout of the GUI

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		layout
			.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
					layout
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(
							layout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(
									layout
										.createSequentialGroup()
										.addComponent(OperatingSystem, javax.swing.GroupLayout.PREFERRED_SIZE,
											javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
								.addGroup(
									layout.createSequentialGroup().addComponent(OSTypeLabel)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(OSType)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(OSVersionLabel)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(OSVersion))
								.addGroup(
									layout.createSequentialGroup()
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(CPU))
								.addGroup(
									layout.createSequentialGroup().addComponent(CPUArchitectureNameLabel)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(CPUArchitectureName))
								.addGroup(
									layout.createSequentialGroup().addComponent(CPUCountLabel)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(CPUCount)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(CPUSpeedLabel)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(CPUSpeed))
								.addGroup(
									layout.createSequentialGroup().addComponent(Memory)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
								.addGroup(
									layout.createSequentialGroup().addComponent(PhysicalMamoryLabel)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(PhysicalMemory)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(VirtualMemoryLabel)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(VirtualMemory))
								.addGroup(
									layout.createSequentialGroup().addComponent(IsAcceptingNewActivities)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
								.addGroup(
									layout.createSequentialGroup().addComponent(CommonNameLabel).addComponent(CommonName)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
								.addGroup(
									layout.createSequentialGroup().addComponent(TotalNumberOfActivitiesLabel)
										.addComponent(TotalNumberOfActivities)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
								.addGroup(
									layout.createSequentialGroup().addComponent(LocalResourceManagerTypeLabel)
										.addComponent(LocalResourcemanagerType)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
								.addGroup(
									layout.createSequentialGroup().addComponent(NamingProfileLabel).addComponent(NamingProfile)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
								.addGroup(
									layout.createSequentialGroup().addComponent(OKButton)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(CancelButton))).addContainerGap(27, Short.MAX_VALUE)));

		layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] { OSTypeLabel, OSType, OSVersionLabel,
			OSVersion });
		layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] { CPUCountLabel, CPUCount,
			CPUSpeedLabel, CPUSpeed });
		layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] { CPUArchitectureNameLabel,
			CPUArchitectureName });
		layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] { PhysicalMamoryLabel, PhysicalMemory,
			VirtualMemoryLabel, VirtualMemory });
		layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] { CommonNameLabel, CommonName });
		layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] { TotalNumberOfActivitiesLabel,
			TotalNumberOfActivities });
		layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] { LocalResourceManagerTypeLabel,
			LocalResourcemanagerType });
		layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] { NamingProfileLabel, NamingProfile });

		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
			layout
				.createSequentialGroup()
				.addContainerGap()
				.addGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(OperatingSystem,
						javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
						javax.swing.GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(OSTypeLabel)
						.addComponent(OSType).addComponent(OSVersionLabel).addComponent(OSVersion))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(CPU))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
						.addComponent(CPUArchitectureNameLabel).addComponent(CPUArchitectureName))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(CPUCountLabel)
						.addComponent(CPUCount).addComponent(CPUSpeedLabel).addComponent(CPUSpeed))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(Memory))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(PhysicalMamoryLabel)
						.addComponent(PhysicalMemory).addComponent(VirtualMemoryLabel).addComponent(VirtualMemory))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
						IsAcceptingNewActivities))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(CommonNameLabel)
						.addComponent(CommonName))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
						.addComponent(TotalNumberOfActivitiesLabel).addComponent(TotalNumberOfActivities))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
						.addComponent(LocalResourceManagerTypeLabel).addComponent(LocalResourcemanagerType))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(NamingProfileLabel)
						.addComponent(NamingProfile))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(
					layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(OKButton)
						.addComponent(CancelButton)).addContainerGap(21, Short.MAX_VALUE)));
		pack();
		toFront();
	}

	protected void submitButtonActionPerformed(ActionEvent evt)
	{

		/*
		 * if the user wants to submit the so-formed query the values entered in the GUI are stored
		 * in the variable queryParameters
		 */
		if (evt.getSource() == OKButton) {
			queryParameters.setOSTypevalue(OSType.getText());
			queryParameters.setOSVersionValue(OSVersion.getText());
			queryParameters.setCPUArchitectureNameValue(CPUArchitectureName.getText());
			queryParameters.setCPUCountValue(CPUCount.getText());
			queryParameters.setCPUSpeedValue(CPUSpeed.getText());
			queryParameters.setPhysicalMemoryValue(PhysicalMemory.getText());
			queryParameters.setVirtualMemoryValue(VirtualMemory.getText());
			queryParameters.setCommonNameValue(CommonName.getText());
			queryParameters.setTotalNumberOfActivitiesValue(TotalNumberOfActivities.getText());
			queryParameters.setLocalResourceManagervalue(LocalResourcemanagerType.getText());
			queryParameters.setNamingProfileValue(NamingProfile.getText());
			queryParameters.setIsAcceptingNewActivitiesValue(IsAcceptingNewActivities.isSelected());
		}

		// if the user decides to cancel the query
		else if (evt.getSource() == CancelButton) {
			System.out.println("Action has been canceled");
		}

		dispose();
	}

	public GUIInternalStruct runGUI()
	{
		new QueryGUI().setVisible(true);
		boolean stayInLoop = true;

		/*
		 * loop until either the OK or the Cancel button of the GUI has been clicked on
		 */
		while (stayInLoop) {
			stayInLoop = readyToReturn;

		}
		readyToReturn = true;
		return queryParameters;
	}

}
