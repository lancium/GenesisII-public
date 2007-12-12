package edu.virginia.vcgr.genii.client.security.gamlauthz.assertions;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

public class BasicConstraints implements AttributeConstraints {

	protected long _notValidBeforeMillis;
	protected long _durationValidMillis;
	protected long _maxDelegationDepth;
	
	// zero-arg contstructor for externalizable use only!
	public BasicConstraints() {}
	
	public BasicConstraints(long notValidBeforeMillis, long durationValidMillis, long maxDelegationDepth) {
		_durationValidMillis = durationValidMillis;
		_notValidBeforeMillis = notValidBeforeMillis;
		_maxDelegationDepth = maxDelegationDepth;
	}	
	
	/**
	 * Checks that the attribute is time-valid with respect to the supplied 
	 * date and any delegation depth requirements are met by the supplied
	 * delegationDepth.
	 */
	public void checkValidity(int delegationDepth, Date date) throws AttributeInvalidException {
		long currentTime = System.currentTimeMillis();
		
		if (currentTime < _notValidBeforeMillis) {
			throw new AttributeNotYetValidException("Security attribute is not yet valid");
		}

		if (currentTime > _notValidBeforeMillis + _durationValidMillis) {
			throw new AttributeExpiredException("Security attribute has expired");
		}
		
		if (delegationDepth > _maxDelegationDepth) {
			throw new AttributeInvalidException("Security attribute exceeds maximum delegation depth");
		}
	}	
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(_durationValidMillis);
		out.writeLong(_notValidBeforeMillis);
		out.writeLong(_maxDelegationDepth);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		_durationValidMillis = in.readLong();
		_notValidBeforeMillis = in.readLong();
		_maxDelegationDepth = in.readLong();
	}	
	
	public String toString() {
		return "Constraints (" + 
			" beforeMillis: " + this._notValidBeforeMillis + 
			" durationValidMillis: " + this._durationValidMillis + 
			" maxDelegationDepth: " + this._maxDelegationDepth + ")"; 
	}
	
}
