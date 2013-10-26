package edu.virginia.vcgr.appmgr.patch.builder.tree.restrictions;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

import edu.virginia.vcgr.appmgr.patch.PatchRestrictions;

public class PatchRestrictionsDialog extends JDialog
{
	static final long serialVersionUID = 0L;

	private PatchRestrictions _restrictions = null;

	private RestrictionsPanel _panel;

	public PatchRestrictionsDialog(JFrame parent, PatchRestrictions restrictions)
	{
		super(parent);
		setTitle("Patch Restrictions");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		Container content = getContentPane();
		content.setLayout(new GridBagLayout());

		content.add(_panel = new RestrictionsPanel(restrictions), new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new OKAction()), new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new CancelAction()), new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
	}

	public PatchRestrictions getRestrictions()
	{
		return _restrictions;
	}

	private class OKAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		static final private String NAME = "OK";

		public OKAction()
		{
			super(NAME);
		}

		@Override
		public void actionPerformed(ActionEvent event)
		{
			_restrictions = _panel.getRestrictions();
			dispose();
		}
	}

	private class CancelAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		static final private String NAME = "Cancel";

		public CancelAction()
		{
			super(NAME);
		}

		@Override
		public void actionPerformed(ActionEvent event)
		{
			_restrictions = null;
			dispose();
		}
	}
}