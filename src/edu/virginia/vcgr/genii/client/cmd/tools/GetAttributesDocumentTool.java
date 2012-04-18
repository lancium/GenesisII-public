package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocument;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse;
import org.w3c.dom.NodeList;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.FileResource;

public class GetAttributesDocumentTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dgetattributes";
	static final private String _USAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/uget-attributes";
	static final private String _MANPAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/man/get-attributes";
	
	private boolean _human = false;
	private boolean _local = false;
	
	public GetAttributesDocumentTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE),
				false, ToolCategory.ADMINISTRATION);
		addManPage(new FileResource(_MANPAGE));
	}
	
	@Option({"human", "h"})
	public void setHuman()
	{
		_human = true;
	}
	
	@Option({"local", "l"})
	public void setLocal()
	{
		_human = true;
		_local = true;
	}

	@Override
	protected int runCommand() throws Throwable
	{
		GeniiPath gPath = new GeniiPath(getArgument(0));
		if ( gPath.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException("<target> must be a grid path. ");
		RNSPath path = lookup(gPath, RNSPathQueryFlags.MUST_EXIST);
		
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class,
			path.getEndpoint());
		GetResourcePropertyDocumentResponse resp = common.getResourcePropertyDocument(
			new GetResourcePropertyDocument());
		if (_human)
		{
			SortedMap<String, String> sortMap = new TreeMap<String, String>();
			for (MessageElement child : resp.get_any())
			{
				String name = (_local ? child.getName() : child.getQName().toString());
				String value = child.getValue();
				if (value == null)
				{
					NodeList nodeList = child.getChildNodes();
					if (nodeList != null)
						value = "<" + nodeList.getLength() + ">";
				}
				sortMap.put(name, value);
			}
			for (Map.Entry<String, String> entry : sortMap.entrySet())
			{
				stdout.println(entry.getKey() + "=" + entry.getValue());
			}
		}
		else
		{
			MessageElement document = new MessageElement(
					new QName(GenesisIIConstants.GENESISII_NS, "attributes"));
			for (MessageElement child : resp.get_any())
			{
				document.addChild(child);
			}
			stdout.println(document);
		}
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException();
	}
}