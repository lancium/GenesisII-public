package edu.virginia.vcgr.genii.ui.rns;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import edu.virginia.vcgr.genii.ui.ApplicationContext;

public class RNSTreeCellRenderer extends DefaultTreeCellRenderer
{
	static final long serialVersionUID = 0L;

	private ApplicationContext _appContext;

	RNSTreeCellRenderer(ApplicationContext appContext)
	{
		_appContext = appContext;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
		int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (value instanceof DefaultMutableTreeNode)
			value = ((DefaultMutableTreeNode) value).getUserObject();

		if (value instanceof RNSTreeObject) {
			RNSTreeObject obj = (RNSTreeObject) value;
			if (obj.objectType() == RNSTreeObjectType.ENDPOINT_OBJECT) {
				RNSFilledInTreeObject fObj = (RNSFilledInTreeObject) obj;
				setIcon(RNSIcons.getIcon(fObj.endpointType(), fObj.isLocal(_appContext)));
			} else if (obj.objectType() == RNSTreeObjectType.EXPANDING_OBJECT) {
				setIcon(RNSIcons.getQuestionIcon());
				Font f = getFont();
				setFont(f.deriveFont(Font.ITALIC));
			} else {
				setIcon(RNSIcons.getErrorIcon());
				Font f = getFont();
				setFont(f.deriveFont(Font.ITALIC));
			}
		}

		return this;
	}
}