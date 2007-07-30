package edu.virginia.vcgr.genii.client.cmd.tools;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.security.gamlauthz.GamlClientTool;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rattrs.GetAttributesResponse;
import edu.virginia.vcgr.genii.common.rattrs.SetAttributes;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;

public class GamlChmodTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Sets read/write/execute GAML authZ permissions for a target.";
	static final private String _USAGE =
		"gaml-chmod <target> " + GamlClientTool.CHMOD_SYNTAX;
	
	static final private int _NEXT_ELEMENT_TARGET = 0;
	static final private int _NEXT_ELEMENT_PERMISSION = 1;
	static final private int _NEXT_ELEMENT_SOURCE = 2;
	
	private String _target = null;
	private String _permission = null;
	private String _user = null;
	private boolean _localSrc = false;
	private boolean _everyone = false;
	
	private int _nextElement = 0;
	
	public GamlChmodTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	public void setLocal_src()
	{
		_localSrc = true;
	}
	
	public void setEveryone()
	{
		_everyone = true;
	}

	@Override
	public void addArgument(String argument) throws ToolException
	{
		if (argument.equals("--everyone"))
		{
			setEveryone();
			return;
		}
		
		if (argument.equals("--local-src"))
		{
			setLocal_src();
			return;
		}
		
		switch (_nextElement)
		{
			case _NEXT_ELEMENT_TARGET :
				_target = argument;
				break;
			case _NEXT_ELEMENT_PERMISSION :
				_permission = argument;
				break;
			case _NEXT_ELEMENT_SOURCE :
				_user = argument;
				break;
			default :
				throw new InvalidToolUsageException();
		}
		
		_nextElement++;
	}

	@Override
	protected int runCommand() throws Throwable
	{
		// create a proxy to the target
		RNSPath path = RNSPath.getCurrent();
		path = path.lookup(_target, RNSPathQueryFlags.MUST_EXIST);
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class,
			path.getEndpoint());

		// get the authz config from the target's attributes
		AuthZConfig config = null;
		GetAttributesResponse resp = common.getAttributes(
				new QName[] {GenesisIIConstants.AUTHZ_CONFIG_ATTR_QNAME});
		MessageElement []elements = resp.get_any();
		if (elements != null && elements.length >= 1) {
			config = (AuthZConfig) elements[0].getObjectValue(
					AuthZConfig.class);
		}
		
		GamlClientTool clientTool = new GamlClientTool();
		if (config == null) {
			config = clientTool.getEmptyAuthZConfig();
		}
		
		config = clientTool.chmod(config,
			_localSrc, _everyone, _permission, _user);
		
		// upload new authz config to resource
		elements = new MessageElement[1];
		elements[0] = new MessageElement(AuthZConfig.getTypeDesc().getXmlType(), config);
		SetAttributes request = new SetAttributes(elements);
		common.setAttributes(request);
		
		return 0;
	}
	
	@Override
	protected void verify() throws ToolException
	{
		if (_target == null)
			throw new InvalidToolUsageException("Target not specified.");
		if (_permission == null)
			throw new InvalidToolUsageException("Permissions not specified.");
		if (_user == null && !_everyone)
			throw new InvalidToolUsageException("No user given.");
	}
}