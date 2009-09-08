package edu.virginia.vcgr.genii.ui.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

import edu.virginia.vcgr.genii.ui.UIContext;

class XMLTreeNodeDocumentRoot extends DefaultMutableTreeNode
	implements XMLTreeNode
{
	static final long serialVersionUID = 0L;
	
	private UIContext _context;
	
	XMLTreeNodeDocumentRoot(UIContext context, String name, 
		XMLTreeSource source)
	{
		super(name);
		
		setAllowsChildren(true);
		add(new XMLProcessingTreeNode("Acquiring XML document"));

		_context = context;
		context.executor().submit(new XMLReaderAcquirer(source));
	}
	
	private class XMLReaderAcquirer implements Runnable
	{
		private XMLTreeSource _source;
		
		private XMLReaderAcquirer(XMLTreeSource source)
		{
			_source = source;
		}
		
		@Override
		public void run()
		{
			try
			{
				new XMLReaderAcquirerReporter(_source.getReader()).run();
			}
			catch (Throwable cause)
			{
				new XMLReaderAcquirerReporter(String.format(
					"Unable to load XML document:  %s",
					cause.getLocalizedMessage())).run();
			}
		}
	}
	
	private class XMLReaderAcquirerReporter implements Runnable
	{
		private String _errorMessage = null;
		private XMLEventReader _reader = null;
		
		private XMLReaderAcquirerReporter(String error)
		{
			_errorMessage = error;
		}
		
		private XMLReaderAcquirerReporter(XMLEventReader reader)
		{
			_reader = reader;
		}
		
		@Override
		public void run()
		{
			if (!SwingUtilities.isEventDispatchThread())
			{
				SwingUtilities.invokeLater(this);
				return;
			}
			
			removeAllChildren();
			if (_errorMessage != null)
			{
				add(new XMLErrorTreeNode(_errorMessage));
			} else
			{
				add(new XMLProcessingTreeNode("Processing XML document"));
				_context.executor().submit(new XMLProcessor(_reader));
			}
		}
	}
	
	private class XMLProcessor implements Runnable
	{
		private XMLEventReader _reader;
		
		private XMLProcessor(XMLEventReader reader)
		{
			_reader = reader;
		}
		
		@Override
		public void run()
		{
			LinkedList<DefaultMutableTreeNode> nodeStack = 
				new LinkedList<DefaultMutableTreeNode>();
			DefaultMutableTreeNode root = null;
			
			XMLEvent event;
			
			try
			{
				while (_reader.hasNext())
				{
					event = _reader.nextEvent();
					switch (event.getEventType())
					{
						case XMLEvent.START_ELEMENT :
							XMLElementTreeNode newNode = new XMLElementTreeNode(
								event.asStartElement());
							if (!nodeStack.isEmpty())
								nodeStack.peek().add(newNode);
							else
								root = newNode;
							
							nodeStack.push(newNode);
							break;
							
						case XMLEvent.CHARACTERS :
							if (nodeStack.isEmpty())
							{
								new XMLProcessorResult(
									new XMLErrorTreeNode(
										"Badly formed XML document")).run();
								return;
							}
							
							String data = event.asCharacters().getData().trim();
							if (data.length() > 0)
							{
								XMLTextContentTreeNode newTNode = 
									new XMLTextContentTreeNode(
										event.asCharacters().getData());
								nodeStack.peek().add(newTNode);
							}
							break;
							
						case XMLEvent.END_ELEMENT :
							if (nodeStack.isEmpty())
							{
								new XMLProcessorResult(
									new XMLErrorTreeNode(
										"Badly formed XML document")).run();
								return;
							}
							
							nodeStack.pop();
							break;
					}
				}
				
				if (root == null)
					new XMLProcessorResult(
						new XMLErrorTreeNode(
							"Badly formed XML document")).run();
				else
				{
					new XMLProcessorResult(
						root.children()).run();
				}
			}
			catch (Throwable cause)
			{
				new XMLProcessorResult(
					new XMLErrorTreeNode(String.format(
						"Unable to parse XML document:  %s", 
						cause.getLocalizedMessage()))).run();
			}
			finally
			{
				if (_reader != null)
					try { _reader.close(); } catch (Throwable cc) {}
			}
		}
	}
	
	private class XMLProcessorResult implements Runnable
	{
		private Collection<DefaultMutableTreeNode> _nodes;
		
		private XMLProcessorResult(DefaultMutableTreeNode node)
		{
			_nodes = new Vector<DefaultMutableTreeNode>();
			_nodes.add(node);
		}
		
		private XMLProcessorResult(Enumeration<?> children)
		{
			_nodes = new ArrayList<DefaultMutableTreeNode>();
			
			while (children.hasMoreElements())
			{
				DefaultMutableTreeNode next = 
					(DefaultMutableTreeNode)children.nextElement();
				_nodes.add(next);
			}
		}
		
		@Override
		public void run()
		{
			if (!SwingUtilities.isEventDispatchThread())
			{
				SwingUtilities.invokeLater(this);
				return;
			}
			
			removeAllChildren();
			for (DefaultMutableTreeNode node : _nodes)
				add(node);
		}
	}

	@Override
	public String asString(String tabs)
	{
		StringBuilder builder = new StringBuilder();
		
		for (int lcv = 0; lcv < getChildCount(); lcv++)
		{
			Object obj = getChildAt(lcv);
			if (obj instanceof XMLTreeNode)
			{
				if (builder.length() > 0)
					builder.append("\n");
				
				builder.append(((XMLTreeNode)obj).asString(tabs));
			}
		}
		
		return builder.toString();
	}
}