package edu.virginia.vcgr.genii.client.cmd.tools.xscript.grid;

import javax.script.ScriptException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.virginia.vcgr.xscript.ParseContext;
import edu.virginia.vcgr.xscript.ParseHandler;
import edu.virginia.vcgr.xscript.ParseStatement;

public class GridParseHandler implements ParseHandler
{
	static public String GRID_NS = 
		"http://vcgr.cs.virginia.edu/genii/xsh/grid";
	
	@Override
	public ParseStatement parse(ParseContext context, Element element)
			throws ScriptException
	{
		GridCommandStatement stmt = new GridCommandStatement(
			element.getLocalName());
		
		NodeList children = element.getChildNodes();
		int length = children.getLength();
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node n = children.item(lcv);
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				Element child = (Element)n;
				stmt.addArgument(
					context.findHandler(child.getNamespaceURI()).parse(
						context, child));
			}
		}
		
		return stmt;
	}
}