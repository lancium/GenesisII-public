package edu.virginia.vcgr.genii.gjt.data.stage.grid;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.gjt.data.stage.StageEditor;
import edu.virginia.vcgr.genii.gjt.gui.util.ButtonPanel;

class GridStageEditor extends StageEditor<GridStageData>
{
	static final long serialVersionUID = 0L;

	private JTextField _path = new JTextField(32);

	@Override
	protected GridStageData getStageDataImpl()
	{
		GridStageData ret = new GridStageData();
		ret.path(_path.getText());
		// ASG 9-06-2014. Now check if the indicated path exists, or how much of the path does
		// anyway
		String pathString = _path.getText();
		boolean exists = true;
		boolean isByteIO = false;
		RNSPath path = RNSPath.getCurrent();
		// Let's see if we can work out the prefix
		String goodPath = path.getValidPrefix(pathString);
		/*
		 * System.out.println("Starting stage out processing "); System.out.println("pathString = "
		 * + pathString); System.out.println("valid prefix is " + goodPath);
		 */
		try {
			path = path.lookup(pathString, RNSPathQueryFlags.MUST_EXIST);
		} catch (RNSPathDoesNotExistException r) {
			exists = false;
		} catch (RNSPathAlreadyExistsException er) {
			exists = true;
		}

		if (exists)
			isByteIO = path.isByteIO();
		// We've now got the prefix and know if it exists or not. If it exists path refers to it.
		if (_stageIn) {
			if (exists && isByteIO)
				return ret;
			// Need to warn them and print out prefix
			String errString = "";
			if (!exists)
				errString =
					"WARNING!!\n Path " + pathString + " does not exist OR you do not have permission.\n The prefix ["
						+ goodPath + "] exists.\nThis will likely cause your job to fail!";
			else if (!isByteIO)
				errString =
					"WARNING!!\n Path " + pathString + " is NOT a ByteIO file\nThis will likely cause your job to fail!";
			// Note it cannot be a byteio and not exists
			JOptionPane.showMessageDialog(null, errString, "Click to continue", JOptionPane.WARNING_MESSAGE);
		} else {
			// This is a stage out. Need to make sure the target directory exists and is a directory
			String errString = "Generic Error";
			if (exists) {
				if (!path.isByteIO()) {
					errString =
						"WARNING!!\n Path " + pathString + " exists and is not a file.\nIf it exists it should be a file.\n"
							+ "This will likely cause your job to fail!";
				} else
					errString =
						"WARNING!!\n Path " + pathString + " already exists and will be overwritten.\n"
							+ "Make sure this is what you intend!";
				if (path.isRNS()) {
					errString =
						"WARNING!!\n Path " + pathString
							+ " is a DIRECTORY. Directories should not be targets.\nThis will likely cause your job to fail!";
				}
				JOptionPane.showMessageDialog(null, errString, "Click to continue", JOptionPane.WARNING_MESSAGE);
			} else {
				// The file does not exists ... that is ok as long as its parent is an RNS
				// The file does not exist, or some part of the path does not exist. This is more
				// complicated
				int baseOffset = pathString.lastIndexOf("/");
				if (baseOffset >= 0) {
					// Everything to the left is the path
					String parentPath = pathString.substring(0, baseOffset);
					/*
					 * System.out.println("parent path = " + parentPath);
					 * System.out.println("valid prefix is " + goodPath);
					 */
					if (parentPath.compareToIgnoreCase(goodPath) != 0) {
						errString =
							"WARNING!!\n Path " + pathString
								+ " does not exist, its parent does not exist, OR you do not have permission.\n The prefix ["
								+ goodPath + "] exists.\nThis will likely cause your job to fail!";
						JOptionPane.showMessageDialog(null, errString, "Click to continue", JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		}
		return ret;
	}

	GridStageEditor(Window owner)
	{
		super(owner, "Grid Data Stage Editor");
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());

		content.add(new JLabel("Grid Path"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		content.add(_path, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

		// content.add(new BrowseRNSPathAction(container,"Path to file", _path, "Another Path"),new
		// GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
		// GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

		JButton okButton = new JButton(createDefaultOKAction());
		getRootPane().setDefaultButton(okButton);
		content.add(ButtonPanel.createHorizontalPanel(okButton, createDefaultCancelAction()), new GridBagConstraints(0, 3, 2,
			1, 1.0, 1.0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
	}

	@Override
	public void setInitialData(GridStageData stageData)
	{
		_path.setText(stageData.path());
	}
}