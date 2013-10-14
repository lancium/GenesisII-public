package edu.virginia.vcgr.appmgr.patch.builder;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.patch.builder.tree.PatchTree;

public class PatchDialog extends JFrame
{
	static final long serialVersionUID = 0L;
	static private final Dimension DEFAULT_SIZE = new Dimension(300, 300);
	static private Log _logger = LogFactory.getLog(PatchDialog.class);

	private File _sourceDirectory;
	private File _patchFile;
	private PatchTree _tree;

	public PatchDialog(PatchRC rc, File sourceDirectory, File patchFile, Map<String, PatchAtom> atoms)
	{
		super("Patch Builder");
		_sourceDirectory = sourceDirectory;
		_patchFile = patchFile;

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Container container = getContentPane();
		container.setLayout(new GridBagLayout());

		_tree = new PatchTree(rc, atoms);
		JScrollPane scroller = new JScrollPane(_tree);

		scroller.setMinimumSize(DEFAULT_SIZE);
		scroller.setPreferredSize(DEFAULT_SIZE);

		container.add(scroller, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		container.add(new JButton(new GenerateAction()), new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
	}

	private class GenerateAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		static private final String NAME = "Generate Patch";

		public GenerateAction()
		{
			super(NAME);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			try {
				_logger.info(String.format("Generating patch file \"%s\".\n", _patchFile));
				Patch p = new Patch(_tree);
				p.generate(PatchDialog.this, _sourceDirectory, _patchFile);
			} catch (IOException ioe) {
				_logger.error("Unable to generate patch.", ioe);
			}

			dispose();
		}
	}
}