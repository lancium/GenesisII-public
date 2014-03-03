package edu.virginia.vcgr.genii.container.wsrf.wsn.topic.xml;

import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;

class TopicTree
{
	TopicTreeNode _root;

	TopicTree()
	{
		_root = new TopicTreeNode();
	}

	final void addTopic(TopicPath path)
	{
		LinkedList<QName> topicPath = path.pathComponents();
		topicPath.removeFirst();
		TopicTreeNode node = _root;

		while (!topicPath.isEmpty()) {
			QName component = topicPath.removeFirst();
			node = node.child(component);
		}

		node.isTopic();
	}

	final void describe(Element rootDocument)
	{
		_root.describe(rootDocument);
	}
}