package edu.virginia.vcgr.genii.ui.xml;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.stream.XMLEventReader;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.ui.UIContext;

public class XMLTree extends JTree
{
	static final long serialVersionUID = 0L;
	
	private RootXMLTreeNode _root;
	
	public XMLTree(String rootTitle)
	{
		super(new RootXMLTreeNode(rootTitle));
		
		setCellRenderer(new XMLTreeCellRenderer());
		
		// setRootVisible(false);
		setShowsRootHandles(true);
		
		_root = (RootXMLTreeNode)getModel().getRoot();
	}
	
	public void add(UIContext context, String treeName, XMLTreeSource source)
	{
		remove(treeName);
		_root.add(new XMLTreeNodeDocumentRoot(context, treeName, source));
	}
	
	public void add(UIContext context, String treeName, XMLEventReader reader)
	{
		remove(treeName);
		_root.add(new XMLTreeNodeDocumentRoot(context, treeName,
			new DefaultXMLTreeSource(reader)));
	}
	
	public void add(UIContext context, String treeName,
		MessageElement me)
	{
		remove(treeName);
		_root.add(new XMLTreeNodeDocumentRoot(context, treeName,
			new DefaultXMLTreeSource(me)));
	}
		
	public void add(UIContext context, String treeName,
		Object serializableObject)
	{
		remove(treeName);
		_root.add(new XMLTreeNodeDocumentRoot(context, treeName,
			new DefaultXMLTreeSource(serializableObject)));
	}
	
	public void remove(String name)
	{
		for (int lcv = 0; lcv < _root.getChildCount(); lcv++)
		{
			String nodeName = 
				(String)((DefaultMutableTreeNode)_root.getChildAt(
					lcv)).getUserObject();
			if (nodeName.equals(name))
			{
				_root.remove(lcv);
				return;
			}
		}
	}
	
	static private class RootXMLTreeNode extends DefaultMutableTreeNode
	{
		private static final long serialVersionUID = 1L;
		
		private RootXMLTreeNode(String rootTitle)
		{
			super(rootTitle);
			setAllowsChildren(true);
		}
	}
}