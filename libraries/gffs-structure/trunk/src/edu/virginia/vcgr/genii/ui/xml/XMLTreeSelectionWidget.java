package edu.virginia.vcgr.genii.ui.xml;

import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Document;
import javax.swing.tree.TreePath;

public class XMLTreeSelectionWidget extends JTextArea implements TreeSelectionListener
{
	static final long serialVersionUID = 0L;

	@Override
	public void valueChanged(TreeSelectionEvent e)
	{
		Document doc = getDocument();
		try {
			doc.remove(0, doc.getLength());
		} catch (Throwable cause) {
		}

		TreePath[] paths = ((JTree) e.getSource()).getSelectionPaths();
		if (paths != null) {
			for (TreePath path : paths) {
				Object obj = path.getLastPathComponent();
				if (obj instanceof XMLTreeNode)
					append(((XMLTreeNode) obj).asString(""));
			}
		}

		setCaretPosition(0);
	}
}