package edu.virginia.vcgr.genii.ui.xml;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.namespace.QName;

class QNameBasedTreeNode extends DefaultMutableTreeNode implements XMLTreeNode
{
	static final long serialVersionUID = 0L;

	static private String formName(QName name)
	{
		String uri = name.getNamespaceURI();
		if (uri == null || uri.length() <= 0)
			return name.getLocalPart();
		else
			return String.format("{%s}%s", uri, name.getLocalPart());
	}

	protected QNameBasedTreeNode(QName name)
	{
		super(formName(name), true);
	}

	@Override
	public String asString(String tabs)
	{
		String name = getUserObject().toString();
		int last = name.indexOf('}');
		if (last >= 0)
			return name.substring(last + 1);
		else
			return name;
	}
}