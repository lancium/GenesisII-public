package edu.virginia.vcgr.genii.client.security.gamlauthz.assertions;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.GeneralSecurityException;
import java.util.Date;

import edu.virginia.vcgr.genii.client.security.VerbosityLevel;

public class BasicConstraints implements AttributeConstraints, Renewable
{

	static public final long serialVersionUID = 5685430283725684151L;

	protected long _notValidBeforeMillis;
	protected long _durationValidMillis;
	protected long _maxDelegationDepth;

	// zero-arg contstructor for externalizable use only!
	public BasicConstraints()
	{
	}

	public BasicConstraints(long notValidBeforeMillis,
			long durationValidMillis, long maxDelegationDepth)
	{
		_durationValidMillis = durationValidMillis;
		_notValidBeforeMillis = notValidBeforeMillis;
		_maxDelegationDepth = maxDelegationDepth;
	}

	/**
	 * Renew this object
	 */
	public void renew() throws GeneralSecurityException
	{
		// renew the start-valid date (minus 10 seconds for consistency)
		_notValidBeforeMillis = System.currentTimeMillis() - (1000 * 10);
	}

	/**
	 * Checks that the attribute is time-valid with respect to the supplied date
	 * and any delegation depth requirements are met by the supplied
	 * delegationDepth.
	 */
	public void checkValidity(int delegationDepth, Date date)
			throws AttributeInvalidException
	{
		long currentTime = System.currentTimeMillis();

		if (currentTime < _notValidBeforeMillis)
		{
			throw new AttributeNotYetValidException(
					"Security attribute is not yet valid");
		}

		if (currentTime > _notValidBeforeMillis + _durationValidMillis)
		{
			throw new AttributeExpiredException(
					"Security attribute has expired");
		}

		if (delegationDepth > _maxDelegationDepth)
		{
			throw new AttributeInvalidException(
					"Security attribute exceeds maximum delegation depth");
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeLong(_durationValidMillis);
		out.writeLong(_notValidBeforeMillis);
		out.writeLong(_maxDelegationDepth);
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException
	{
		_durationValidMillis = in.readLong();
		_notValidBeforeMillis = in.readLong();
		_maxDelegationDepth = in.readLong();
	}

	public String toString()
	{
		/*
		long days = _durationValidMillis / (1000 * 60 * 60 * 24);
		long remainder = _durationValidMillis % (1000 * 60 * 60 * 24);
		long hours = remainder / (1000 * 60 * 60);
		remainder = remainder % (1000 * 60 * 60);
		long minutes = remainder / (1000 * 60);
		remainder = remainder % (1000 * 60);
		long seconds = remainder / (1000);
		remainder = remainder % (1000);

		return "Constraints (" + " before: \""
				+ (new Date(_notValidBeforeMillis)) + "\" duration: \"" + days
				+ " days, " + hours + " hours, " + minutes + " minutes, "
				+ seconds + " seconds" + "\" maxDelegationDepth: "
				+ this._maxDelegationDepth + ")";
		*/
		return describe(VerbosityLevel.HIGH);
	}
	
	@Override
	public String describe(VerbosityLevel verbosity)
	{
		Date before = new Date(_notValidBeforeMillis);
		Date after = new Date(_notValidBeforeMillis + _durationValidMillis);
		
		return String.format("[%1$tD %1$tT, %2$tD %2$tT]",
			before, after);
	}

	@Override
	public Date getExpiration()
	{
		return new Date(_notValidBeforeMillis + _durationValidMillis);
	}
}
