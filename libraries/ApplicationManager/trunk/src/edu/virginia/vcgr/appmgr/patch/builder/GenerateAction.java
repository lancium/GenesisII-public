package edu.virginia.vcgr.appmgr.patch.builder;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.patch.builder.tree.PatchTree;

public class GenerateAction extends AbstractAction
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(GenerateAction.class);

	static private final String NAME = "Generate Patch";

	private JFrame _application;
	private PatchTree _tree;

	public GenerateAction(JFrame application, PatchTree tree)
	{
		super(NAME);

		_application = application;
		_tree = tree;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		_logger.info("Should generate now.");
		PatchDescription description = new PatchDescription(_tree);
		description.emit(System.out);
		_application.dispose();
	}
}