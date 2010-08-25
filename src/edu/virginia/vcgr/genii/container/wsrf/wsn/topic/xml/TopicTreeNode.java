package edu.virginia.vcgr.genii.container.wsrf.wsn.topic.xml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

class TopicTreeNode
{
	private boolean _isTopic = false;
	private Map<QName, TopicTreeNode> _children = 
		new HashMap<QName, TopicTreeNode>();
	
	TopicTreeNode()
	{
	}
	
	TopicTreeNode child(QName childName)
	{
		TopicTreeNode ret = _children.get(childName);
		if (ret == null)
			_children.put(childName, ret = new TopicTreeNode());
		
		return ret;
	}
	
	final void isTopic()
	{
		_isTopic = true;
	}
	
	final void describe(Element node)
	{
		if (_isTopic)
			node.setAttribute("topic", "true");
		
		Document ownerDocument = node.getOwnerDocument();
		
		for (Map.Entry<QName, TopicTreeNode> child : _children.entrySet())
		{
			QName childName = child.getKey();
			
			Element treeDoc = (Element)node.appendChild(
				ElementBuilderUtils.createElement(ownerDocument, childName));
			child.getValue().describe(treeDoc);
		}
	}
}