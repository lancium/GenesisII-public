package edu.virginia.vcgr.appmgr.patch.builder.tree.restrictions;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.appmgr.os.ProcessorArchitecture;
import edu.virginia.vcgr.appmgr.patch.HostRestriction;
import edu.virginia.vcgr.appmgr.patch.PatchRestrictions;

@SuppressWarnings("rawtypes")
public class RestrictionsPanel extends JPanel
{
	static final long serialVersionUID = 0L;

	static private JComboBox createOSCombo(OperatingSystemType os)
	{
		List<String> strings = new Vector<String>(OperatingSystemType.values().length + 1);
		strings.add("");
		for (OperatingSystemType type : OperatingSystemType.values())
			strings.add(type.toString());
		Collections.sort(strings);
		@SuppressWarnings("unchecked")
		JComboBox ret = new JComboBox(strings.toArray(new String[strings.size()]));
		if (os != null)
			ret.setSelectedItem(os.toString());
		else
			ret.setSelectedItem("");

		return ret;
	}

	static private JTextField createOSVersionField(String osVersion)
	{
		if (osVersion == null)
			osVersion = "";

		return new JTextField(osVersion, 16);
	}

	static private JComboBox createProcessorArchCombo(ProcessorArchitecture processorArch)
	{
		List<String> strings = new Vector<String>(ProcessorArchitecture.values().length + 1);
		strings.add("");
		for (ProcessorArchitecture type : ProcessorArchitecture.values())
			strings.add(type.toString());
		Collections.sort(strings);
		@SuppressWarnings("unchecked")
		JComboBox ret = new JComboBox(strings.toArray(new String[strings.size()]));
		if (processorArch != null)
			ret.setSelectedItem(processorArch.toString());
		else
			ret.setSelectedItem("");

		return ret;
	}

	static private HostRestrictionPanel createHostRestrictionsPanel(HostRestriction restrictions)
	{
		HostRestrictionPanel panel = new HostRestrictionPanel(restrictions);
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
			"Host Restriction"));
		return panel;
	}

	private JComboBox _osCombo;
	private JTextField _osVersion;
	private JComboBox _processorArchCombo;
	private HostRestrictionPanel _hostRestriction;

	public RestrictionsPanel(PatchRestrictions restrictions)
	{
		super(new GridBagLayout());

		add(new JLabel("Operating System"), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_osCombo = createOSCombo(restrictions.getOSType()), new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Operating System Version"), new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_osVersion = createOSVersionField(restrictions.getOSVersion()), new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Processor Architecture"), new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_processorArchCombo = createProcessorArchCombo(restrictions.getProcessorArchitecture()), new GridBagConstraints(1,
			2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(_hostRestriction = createHostRestrictionsPanel(restrictions.getHostRestriction()), new GridBagConstraints(0, 3, 2,
			1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
	}

	public PatchRestrictions getRestrictions()
	{
		PatchRestrictions restrictions = new PatchRestrictions();

		String os = _osCombo.getSelectedItem().toString();
		String version = _osVersion.getText();
		String pArch = _processorArchCombo.getSelectedItem().toString();

		if (os != null && os.length() > 0)
			restrictions.setOperatingSystemTypeRestriction(OperatingSystemType.valueOf(os));
		if (version != null && version.length() > 0)
			restrictions.setOperatingSystemVersionRestriction(version);
		if (pArch != null && pArch.length() > 0)
			restrictions.setProcessorArchitectureRestriction(ProcessorArchitecture.valueOf(pArch));
		restrictions.setHostRestriction(_hostRestriction.getRestrictions());

		return restrictions;
	}
}