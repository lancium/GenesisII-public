package edu.virginia.vcgr.genii.ui.plugins.logs.tree;

import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import edu.virginia.vcgr.genii.ui.ApplicationContext;
import edu.virginia.vcgr.genii.ui.EndpointType;

public class LogTreeCellRenderer extends DefaultTreeCellRenderer
{
	static final long serialVersionUID = 0L;

	private ApplicationContext _appContext;

	LogTreeCellRenderer(ApplicationContext appContext)
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

		if (value instanceof LogTreeObject) {
			LogTreeObject obj = (LogTreeObject) value;
			if (obj.objectType() == LogTreeObjectType.ENDPOINT_OBJECT) {
				LogFilledInTreeObject fObj = (LogFilledInTreeObject) obj;
				boolean isLocal = fObj.isLocal(_appContext);
				Icon icon = LogIcons.LogIconsFactory().getIcon(EndpointType.DIRECTORY, isLocal);
				// LogIcons.getIcon(fObj.endpointType(), fObj.isLocal(_appContext));
				setIcon(icon);
			} else if (obj.objectType() == LogTreeObjectType.EXPANDING_OBJECT) {
				setIcon(LogIcons.LogIconsFactory().getQuestionIcon());
				Font f = getFont();
				setFont(f.deriveFont(Font.ITALIC));
			} else {
				setIcon(LogIcons.LogIconsFactory().getErrorIcon());
				Font f = getFont();
				setFont(f.deriveFont(Font.ITALIC));
			}
		}

		return this;
	}
}