package edu.virginia.vcgr.genii.container.invoker;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;

import org.oasis_open.wsrf.basefaults.BaseFaultType;

public class BaseFaultFixer implements IAroundInvoker
{
	@Override
	public Object invoke(InvocationContext invocationContext) throws Exception
	{
		try
		{
			return invocationContext.proceed();
		}
		catch (BaseFaultType bft)
		{
			if (bft.getTimestamp() == null)
				bft.setTimestamp(Calendar.getInstance());
			throw bft;
		}
		catch (InvocationTargetException ite)
		{
			Throwable cause = ite.getCause();
			if (cause instanceof Exception)
				throw (Exception)cause;
			else
				throw ite;
		}
	}
}