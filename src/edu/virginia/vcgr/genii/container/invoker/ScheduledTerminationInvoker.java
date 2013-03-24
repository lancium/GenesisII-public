package edu.virginia.vcgr.genii.container.invoker;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;

import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class ScheduledTerminationInvoker implements IAroundInvoker
{
	static private Log _logger = LogFactory.getLog(ScheduledTerminationInvoker.class);

	@Override
	public Object invoke(InvocationContext invocationContext) throws Exception
	{
		IResource resource = ResourceManager.getCurrentResource().dereference();
		Calendar termTime = (Calendar) resource.getProperty(IResource.SCHEDULED_TERMINATION_TIME_PROPERTY_NAME);
		if (termTime != null) {
			if (termTime.before(Calendar.getInstance())) {
				if (_logger.isDebugEnabled())
					_logger.debug("Terminating a scheduled termination resource.");
				((GenesisIIBase) invocationContext.getTarget()).destroy(new Destroy());
				throw FaultManipulator.fillInFault(new ResourceUnknownFaultType(null, null, null, null, null, null));
			}
		}

		return invocationContext.proceed();
	}
}