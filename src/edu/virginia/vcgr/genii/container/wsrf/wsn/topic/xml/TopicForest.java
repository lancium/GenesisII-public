package edu.virginia.vcgr.genii.container.wsrf.wsn.topic.xml;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;

public class TopicForest
{
	private Map<QName, TopicTree> _forest = new HashMap<QName, TopicTree>();

	final public void addTopic(TopicPath topic)
	{
		QName root = topic.root();
		TopicTree tree = _forest.get(root);
		if (tree == null)
			_forest.put(root, tree = new TopicTree());

		tree.addTopic(topic);
	}

	final public void describe(Element rootDocument)
	{
		Document ownerDocument = rootDocument.getOwnerDocument();

		for (Map.Entry<QName, TopicTree> tree : _forest.entrySet()) {
			QName treeName = tree.getKey();

			Element treeDoc = (Element) rootDocument.appendChild(ElementBuilderUtils.createElement(ownerDocument, treeName));
			tree.getValue().describe(treeDoc);
		}
	}
}