package edu.virginia.vcgr.genii.security.credentials;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

import edu.virginia.vcgr.genii.security.Describable;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.faults.AttributeExpiredException;
import edu.virginia.vcgr.genii.security.faults.AttributeInvalidException;
import edu.virginia.vcgr.genii.security.faults.AttributeNotYetValidException;

/**
 * Describes how long a credential can live, when it starts being valid, and the
 * maximum delegation depth allowed on it.
 * 
 * @author dmerrill
 * @author ckoeritz
 */
public class BasicConstraints implements Describable {
	static public final long serialVersionUID = 2L;

	protected long _notValidBeforeMillis;
	protected long _durationValidMillis;
	protected int _maxDelegationDepth;

	// zero-arg constructor for externalizable use only.
	public BasicConstraints() {
	}

	public BasicConstraints(long notValidBeforeMillis,
			long durationValidMillis, int maxDelegationDepth) {
		_durationValidMillis = durationValidMillis;
		_notValidBeforeMillis = notValidBeforeMillis;
		_maxDelegationDepth = maxDelegationDepth;
	}

	/**
	 * Checks that the attribute is time-valid with respect to the supplied date
	 * and any delegation depth requirements are met by the supplied
	 * delegationDepth.
	 */
	public void checkValidity(int delegationDepth, Date date)
			throws AttributeInvalidException {
		long currentTime = System.currentTimeMillis();

		if (currentTime < _notValidBeforeMillis) {
			throw new AttributeNotYetValidException(
					"Security attribute is not yet valid");
		}

		if (currentTime > _notValidBeforeMillis + _durationValidMillis) {
			throw new AttributeExpiredException(
					"Security attribute has expired");
		}

		if (delegationDepth > _maxDelegationDepth) {
			throw new AttributeInvalidException(
					"Security attribute exceeds maximum delegation depth");
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(_durationValidMillis);
		out.writeLong(_notValidBeforeMillis);
		out.writeInt(_maxDelegationDepth);
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		_durationValidMillis = in.readLong();
		_notValidBeforeMillis = in.readLong();
		_maxDelegationDepth = in.readInt();
	}

	public String toString() {
		return describe(VerbosityLevel.HIGH);
	}

	@Override
	public String describe(VerbosityLevel verbosity) {
		Date before = new Date(_notValidBeforeMillis);
		Date after = new Date(_notValidBeforeMillis + _durationValidMillis);

		return String.format("[%1$tF %1$tT, %2$tF %2$tT]", before, after);
	}

	public Date getNotValidBefore() {
		return new Date(_notValidBeforeMillis);
	}

	public int getMaxDelegationDepth() {
		return _maxDelegationDepth;
	}

	public Date getExpiration() {
		return new Date(_notValidBeforeMillis + _durationValidMillis);
	}
}
