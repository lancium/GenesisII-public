package edu.virginia.vcgr.genii.client.cmd.tools.besmgr;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JDialog;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ContextManager;

public class BESManagerDialog extends JDialog
{
	static final long serialVersionUID = 0L;

	public BESManagerDialog(Window owner, EndpointReferenceType target) throws FileNotFoundException, IOException
	{
		super(owner);

		setTitle("BES Manager");

		Container content = getContentPane();
		content.setLayout(new GridBagLayout());

		add(new BESManagerPanel(ContextManager.getExistingContext(), target), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
}