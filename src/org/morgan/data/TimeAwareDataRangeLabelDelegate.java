package org.morgan.data;

import java.util.Calendar;

public interface TimeAwareDataRangeLabelDelegate<Type>
	extends DataRangeLabelDelegate<Type>
{
	public void markTime(Calendar timestamp);
}
