package edu.virginia.vcgr.genii.container.invoker;

import javax.xml.namespace.QName;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.Handler;
import org.apache.axis.deployment.wsdd.WSDDProvider;
import org.apache.axis.deployment.wsdd.WSDDService;

public class GAroundInvokerFactory extends WSDDProvider
{
	static public final String GENII_NS = "http://vcgr.cs.virginia.edu/GenesisII/invoker";
	static public final String NAME = "GAroundInvoker";

	static public QName PROVIDER_QNAME = new QName(GENII_NS, NAME);

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public Handler newProviderInstance(WSDDService service, EngineConfiguration registry) throws Exception
	{
		return new GAroundInvoker();
	}
}