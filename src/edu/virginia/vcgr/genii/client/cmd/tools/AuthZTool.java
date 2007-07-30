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

public class AuthZTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Configures various aspects of security for specified target.";
	static final private String _USAGE =
		"authz <target>";
	
	public AuthZTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		RNSPath path = RNSPath.getCurrent();
		path = path.lookup(getArgument(0), RNSPathQueryFlags.MUST_EXIST);
		
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
		
		boolean done = false;
		while (!done) {

			// display retrieved authz config
			stdout.println("Authorization configuration for " + getArgument(0) + ":\n");
			GamlClientTool subTool = new GamlClientTool();
			if ((config == null) || (config.get_any() == null)) {
				stdout.println("No authorization module set.");
			} else {
				subTool.displayAuthZConfig(config, stdout, stderr, stdin);
			}
				
			boolean chosen = false;
			while (!chosen) {
				stdout.println("\nOptions:");
				stdout.println("  [1] Modify AuthZ config");
				stdout.println("  [2] Submit AuthZ config");
				stdout.println("  [3] Cancel");
				stdout.print("Please make a selection: ");
				stdout.flush();
				
				String input = stdin.readLine();
				stdout.println();
				int choice = 0;					
				try {
					choice = Integer.parseInt(input);
				} catch (NumberFormatException e) {
					stdout.println("Invalid choice.");
					continue;
				}
					
				switch (choice) {
				case 1: 
					// use subtool to modify authz config
					if (subTool == null) {
						stdout.println("Invalid choice.");
					} else {
						config = subTool.modifyAuthZConfig(config, stdout, stderr, stdin);
						chosen = true;
					}
					break;
				case 2: 
					// upload new authz config to resource
					elements = new MessageElement[1];
					elements[0] = new MessageElement(AuthZConfig.getTypeDesc().getXmlType(), config);
					SetAttributes request = new SetAttributes(elements);
					common.setAttributes(request);
					
					chosen = true;
					done = true;
					break;
				case 3:
					// cancel
					chosen = true;
					done = true;
					break;
				default: 
					stdout.println("Invalid choice.");
				}
					
			}
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