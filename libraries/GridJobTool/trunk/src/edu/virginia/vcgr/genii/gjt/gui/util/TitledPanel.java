package edu.virginia.vcgr.genii.gjt.gui.util;

import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

public class TitledPanel extends JPanel
{
	static final long serialVersionUID = 0L;

	public TitledPanel(String title, LayoutManager layout)
	{
		super(layout);

		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), title));
	}

	public TitledPanel(String title)
	{
		super();

		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), title));
	}
}